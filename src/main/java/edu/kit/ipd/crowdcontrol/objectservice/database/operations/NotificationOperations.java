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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION_RECEIVER_EMAIL;

/**
 * Contains all operations needed to interact with notifications in the database.
 *
 * @author Leander K.
 * @author Niklas Keller
 */
public class NotificationOperations extends AbstractOperations {
    private final DSLContext readOnlyCreate;

    /**
     * creates an new instance of NotificationOperation.
     *
     * @param manager  the manager used to obtain all relevant information about the DB
     * @param username the username belonging to a read-only account on the DB
     * @param password the matching password belonging to a read-only account on the DB
     *
     * @throws SQLException if there was a problem establishing a connection to the database
     */
    public NotificationOperations(DatabaseManager manager, String username, String password) throws SQLException {
        super(manager.getContext());

        HikariDataSource readOnlyDataSource = new HikariDataSource();
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
    public List<NotificationRecord> getAllNotifications() {
        return create.selectFrom(Tables.NOTIFICATION)
                .fetch();
    }

    /**
     * Returns a range of notifications starting from {@code cursor}
     *
     * @param cursor pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  the number of records
     *
     * @return a list of notifications
     */
    public Range<Notification, Integer> getNotificationsFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(NOTIFICATION), NOTIFICATION.ID_NOTIFICATION, NOTIFICATION, cursor, next, limit)
                .map(notification -> NotificationTransformer.toProto(notification, Collections.emptyList()));
    }

    /**
     * inserts a notification into the database.
     *
     * @param record the record to insert
     *
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
     * the passed notification must have the following fields set:<br> name, description, query,
     * check_period and send_threshold
     *
     * @param toStore Notification to save
     *
     * @return an instance of notification with ID assigned
     *
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

        NotificationRecord record = NotificationTransformer.mergeRecord(create.newRecord(NOTIFICATION), toStore);
        record.store();

        List<NotificationReceiverEmailRecord> emails = toStore.getEmailsList().stream()
                .map(mail -> {
                    NotificationReceiverEmailRecord receiver = new NotificationReceiverEmailRecord();
                    receiver.setNotification(record.getIdNotification());
                    receiver.setEmail(mail);
                    return receiver;
                })
                .collect(Collectors.toList());

        create.batchInsert(emails).execute();

        return NotificationTransformer.toProto(record, emails);
    }

    /**
     * Deletes a notification.
     *
     * @param id the id of the notification
     *
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
     *
     * @return the notification of empty if not found
     */
    public Optional<Notification> getNotification(int id) {
        return create.fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .map(notificationRecord -> {
                    Result<NotificationReceiverEmailRecord> emails = create.selectFrom(NOTIFICATION_RECEIVER_EMAIL)
                            .where(NOTIFICATION_RECEIVER_EMAIL.NOTIFICATION.eq(id))
                            .fetch();

                    return NotificationTransformer.toProto(notificationRecord, emails);
                });
    }

    /**
     * updates the notifications lastSend field
     *
     * @param notificationID the primary key of the notification
     *
     * @return {@code true} if updated, {@code false} if not found
     */
    public boolean updateLastSendForNotification(int notificationID) {
        //TODO? wait for simon!
        return true;//create.update(Tables.NOTIFICATION)
        //.set(Tables.NOTIFICATION.LASTSENT, Timestamp.valueOf(LocalDateTime.now()))
        //.where(Tables.NOTIFICATION.ID_NOTIFICATION.eq(notificationID))
        //.execute() == 1;
    }

    /**
     * Updates a notification.
     *
     * @param id           ID of the notification
     * @param notification new notification contents
     *
     * @return the updated notification
     */
    public Notification updateNotification(int id, Notification notification) {
        NotificationRecord record = create
                .fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .orElseThrow(() -> new NotFoundException("Notification does not exist!"));

        NotificationTransformer.mergeRecord(record, notification);
        record.update();

        if (!notification.getEmailsList().isEmpty()) {
            List<NotificationReceiverEmailRecord> toInsert = notification.getEmailsList().stream()
                    .map(mail -> {
                        NotificationReceiverEmailRecord receiver = new NotificationReceiverEmailRecord();
                        receiver.setNotification(record.getIdNotification());
                        receiver.setEmail(mail);
                        return receiver;
                    })
                    .collect(Collectors.toList());

            create.transaction(conf -> {
                DSL.using(conf).deleteFrom(NOTIFICATION_RECEIVER_EMAIL)
                        .where(NOTIFICATION_RECEIVER_EMAIL.NOTIFICATION.eq(id))
                        .execute();

                DSL.using(conf).batchInsert(toInsert).execute();
            });
        }

        return this.getNotification(record.getIdNotification()).orElseThrow(NotFoundException::new);
    }

    /**
     * runs a sql-query in read-only mode
     *
     * @param sql the seq to execute in read-only mode
     *
     * @return the Result of the query
     */
    public Result<Record> runReadOnlySQL(String sql) {
        return readOnlyCreate.fetch(sql);
    }
}
