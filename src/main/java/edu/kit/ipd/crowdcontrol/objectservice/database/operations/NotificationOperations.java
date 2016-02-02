package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.zaxxer.hikari.HikariDataSource;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.NotificationTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION;

/**
 * contains all Operations needed to interact with the notifications-table.
 * @author LeanderK
 * @version 1.0
 */
public class NotificationOperations extends AbstractOperations {
    private final DSLContext readOnlyCreate;
    private final HikariDataSource readOnlyDataSource;

    /**
     * creates an new instance of NotificationOperation.
     * @param manager the manager used to obtain all relevant information about the DB
     * @param username the username belonging to a read-only account on the DB
     * @param password the matching password belonging to a read-only accoint on the DB
     * @throws SQLException if there was a problem establishing a connection to the database
     */
    public NotificationOperations(DatabaseManager manager, String username, String password) throws SQLException {
        super(manager.getContext());
        readOnlyDataSource = new HikariDataSource();
        readOnlyDataSource.setJdbcUrl(manager.getUrl());
        readOnlyDataSource.setUsername(username);
        readOnlyDataSource.setPassword(password);
        readOnlyDataSource.setMaximumPoolSize(2);
        //don't trust this!
        readOnlyDataSource.setReadOnly(true);
        readOnlyCreate = DSL.using(readOnlyDataSource, manager.getContext().configuration().dialect());
    }

    /**
     * returns all the stored notifications
     * @return a list of notifications
     */
    public List<NotificationRecord> getAllNotifications() {
        return create.selectFrom(Tables.NOTIFICATION)
                .fetch();
    }

    /**
     * Returns a range of notifications starting from {@code cursor}
     *
     * @param cursor pagination cursor
     * @param next {@code true} for next, {@code false} for previous
     * @param limit the umber of records
     * @return a list of notifications
     */
    public Range<Notification, Integer> getNotificationsFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(NOTIFICATION), NOTIFICATION.ID_NOTIFICATION, NOTIFICATION, cursor, next, limit)
                .map(NotificationTransformer::toProto);
    }

    /**
     * inserts a notification into the database.
     * @param record the record to insert
     * @return the resulting record (the primary key is guaranteed to be set)
     */
    public NotificationRecord insertNotification(NotificationRecord record) {
        return create.insertInto(Tables.NOTIFICATION)
                .set(record)
                .returning()
                .fetchOne();
    }

    /**
     * Creates a new notification.
     * <p>
     * the passed notification must have the following fields set:<br>
     * name, description, query, check_period and send_threshold
     * @param toStore Notification to save
     * @return an instance of notification with ID assigned
     * @throws IllegalArgumentException if one of the specified fields is not set
     */
    public Notification insertNotification(Notification toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Notification.NAME_FIELD_NUMBER,
                Notification.DESCRIPTION_FIELD_NUMBER,
                Notification.QUERY_FIELD_NUMBER,
                Notification.CHECK_PERIOD_FIELD_NUMBER,
                Notification.SEND_THRESHOLD_FIELD_NUMBER);

        NotificationRecord record = NotificationTransformer.mergeRecord(create.newRecord(NOTIFICATION), toStore);
        record.store();

        return NotificationTransformer.toProto(record);
    }

    /**
     * deletes a notification from the database
     * @param notificationID the primary key of the notification
     * @return true if deleted, false if not found
     */
    public boolean delteNotification(int notificationID) {
        return create.deleteFrom(Tables.NOTIFICATION)
                .where(Tables.NOTIFICATION.ID_NOTIFICATION.eq(notificationID))
                .execute() == 1;
    }

    /**
     * Deletes a notification.
     *
     * @param id the id of the notification
     * @return {@code true} if deleted, {@code false} otherwise
     */
    public boolean deleteNotification(int id) {
        NotificationRecord record = create.newRecord(NOTIFICATION);
        record.setIdNotification(id);

        return create.executeDelete(record, NOTIFICATION.ID_NOTIFICATION.eq(id)) == 1;
    }

    /**
     * Returns a single notification.
     *
     * @param id the ID of the notification
     * @return the notification of empty if not found
     */
    public Optional<Notification> getNotification(int id) {
        return create.fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .map(NotificationTransformer::toProto);
    }

    /**
     * updates the notifications lastSend field
     *
     * @param notificationID the primary key of the notification
     * @return true if updated, false if not found
     */
    public boolean updateLastSentForNotification(int notificationID, Instant now) {
        return create.update(Tables.NOTIFICATION)
                .set(Tables.NOTIFICATION.LASTSENT, Timestamp.from(now))
                .where(Tables.NOTIFICATION.ID_NOTIFICATION.eq(notificationID))
                .execute() == 1;
    }

    /**
     * Updates a notification.
     *
     * @param id ID of the notification
     * @param notification new notification contents
     * @return the updated notification
     */
    public Notification updateNotification(int id, Notification notification) {
        NotificationRecord record = create
                .fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .orElseThrow(() -> new NotFoundException("Notification does not exist!"));

        record = NotificationTransformer.mergeRecord(record, notification);
        record.update();

        return NotificationTransformer.toProto(record);
    }

    /**
     * runs a sql-query in read-only mode
     *
     * @param sql the seq to execute in read-only mode
     * @return the Result of the query
     */
    public Result<Record> runReadOnlySQL(String sql) {
        return readOnlyCreate.fetch(sql);
    }
}
