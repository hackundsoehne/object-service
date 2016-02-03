package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.zaxxer.hikari.HikariDataSource;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationReceiverEmailRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.NotificationTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION_RECEIVER_EMAIL;

/**
 * contains all Operations needed to interact with the notifications-table.
 *
 * @author LeanderK
 * @version 1.0
 */
public class NotificationOperations extends AbstractOperations {
    private final DSLContext readOnlyCreate;
    private final HikariDataSource readOnlyDataSource;

    /**
     * creates an new instance of NotificationOperation.
     *
     * @param manager  the manager used to obtain all relevant information about the DB
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
     *
     * @return a list of notifications
     */
    public List<Notification> getAllNotifications() {
        return create.selectFrom(Tables.NOTIFICATION)
                .fetch()
                .map(this::fetchAndAddEmailsToNotification);
    }


    /**
     * Returns a single notification.
     *
     * @param id the ID of the notification
     * @return the notification of empty if not found
     */
    public Optional<Notification> getNotification(int id) {
        return create.fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .map(this::fetchAndAddEmailsToNotification);
    }

    /**
     * Returns a range of notifications starting from {@code cursor}
     *
     * @param cursor pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  the umber of records
     * @return a list of notifications
     */
    public Range<Notification, Integer> getNotificationsFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(NOTIFICATION), NOTIFICATION.ID_NOTIFICATION, NOTIFICATION, cursor, next, limit)
                .map(this::fetchAndAddEmailsToNotification);
    }

    /**
     * Fetches all NotificationReceiverEmailRecords for the given NotificationRecord and
     * adds the emails to the protobuf.
     *
     * @param notificationRecord the NotificationRecord to add the emails to
     * @return a Notification
     */
    private Notification fetchAndAddEmailsToNotification(NotificationRecord notificationRecord) {
        return NotificationTransformer.toProto(notificationRecord,
                create.selectFrom(NOTIFICATION_RECEIVER_EMAIL)
                        .where(NOTIFICATION_RECEIVER_EMAIL.ID_NOTIFICATION_RECEIVER_EMAIL
                                .eq(notificationRecord.getIdNotification()))
                        .fetch());
    }

    /**
     * Creates a new notification.
     * <p>
     * the passed notification must have the following fields set:<br>
     * name, description, query, check_period, emails and send_once
     *
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
                Notification.SEND_ONCE_FIELD_NUMBER,
                Notification.EMAILS_FIELD_NUMBER);

        NotificationRecord notificationRecord = NotificationTransformer.mergeRecord(create.newRecord(NOTIFICATION), toStore);
        notificationRecord.store();

        toStore.getEmailsList().stream()
                .map(s -> new NotificationReceiverEmailRecord(null, notificationRecord.getIdNotification(), s))
                .collect(Collectors.collectingAndThen(Collectors.toList(), create::batchInsert))
                .execute();

        return fetchAndAddEmailsToNotification(notificationRecord);
    }


    /**
     * Deletes a notification.
     *
     * @param id the id of the notification
     * @return {@code true} if deleted, {@code false} otherwise
     */
    public boolean deleteNotification(int id) {
        create.deleteFrom(NOTIFICATION_RECEIVER_EMAIL)
                .where(NOTIFICATION_RECEIVER_EMAIL.NOTIFICATION.eq(id));
        return create.deleteFrom(NOTIFICATION).where(NOTIFICATION.ID_NOTIFICATION.eq(id)).execute() == 1;
    }


    /**
     * Updates a notification.
     *
     * @param id           ID of the notification
     * @param notification new notification contents
     * @return the updated notification
     */
    public Notification updateNotification(int id, Notification notification) {
        NotificationRecord notificationRecord = create
                .fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .orElseThrow(() -> new NotFoundException("Notification does not exist!"));

        notificationRecord = NotificationTransformer.mergeRecord(notificationRecord, notification);
        notificationRecord.update();

        notification.getEmailsList().stream()
                .map(s -> new NotificationReceiverEmailRecord(null, notification.getId(), s))
                .collect(Collectors.collectingAndThen(Collectors.toList(), create::batchUpdate))
                .execute();

        return fetchAndAddEmailsToNotification(notificationRecord);
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
