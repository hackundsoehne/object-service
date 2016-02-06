package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.zaxxer.hikari.HikariDataSource;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationReceiverEmailRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationTokenRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.NotificationTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.apache.commons.validator.routines.EmailValidator;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * Contains all operations needed to interact with notifications in the database.
 *
 * @author Leander K.
 * @author Niklas Keller
 * @author Simon Korz
 */
public class NotificationOperations extends AbstractOperations {
    private final DSLContext readOnlyCreate;

    /**
     * Creates an new instance of NotificationOperation.
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
    public List<Notification> getAllNotifications() {
        return create.selectFrom(Tables.NOTIFICATION).fetch()
                .map(notificationRecord -> NotificationTransformer.toProto(notificationRecord,
                        fetchEmailsForNotification(notificationRecord)));
    }


    /**
     * Returns a single notification.
     *
     * @param id the ID of the notification
     * @return the notification of empty if not found
     */
    public Optional<Notification> getNotification(int id) {
        return create.fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .map(notificationRecord -> NotificationTransformer.toProto(notificationRecord,
                        fetchEmailsForNotification(notificationRecord)));
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
                .map(notificationRecord -> NotificationTransformer.toProto(notificationRecord,
                        fetchEmailsForNotification(notificationRecord)));
    }

    /**
     * Fetches all NotificationReceiverEmailRecords for the given NotificationRecord.
     *
     * @param notificationRecord the NotificationRecord to fetch the emails for
     * @return list of NotificationReceiverEmailRecords
     */
    private List<NotificationReceiverEmailRecord> fetchEmailsForNotification(NotificationRecord notificationRecord) {
        return create.selectFrom(NOTIFICATION_RECEIVER_EMAIL)
                .where(NOTIFICATION_RECEIVER_EMAIL.NOTIFICATION.eq(notificationRecord.getIdNotification()))
                .fetch();
    }

    /**
     * Creates a new notification.
     * <p>
     * The passed notification must have the following fields set:<br>
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

        toStore.getEmailsList().forEach(email -> {
            if (!EmailValidator.getInstance(false).isValid(email)) {
                throw new IllegalArgumentException(String.format("Invalid email: %s", email));
            }
        });

        NotificationRecord notificationRecord = NotificationTransformer.mergeRecord(create.newRecord(NOTIFICATION), toStore);
        notificationRecord.store();

        toStore.getEmailsList().stream()
                .map(s -> new NotificationReceiverEmailRecord(null, notificationRecord.getIdNotification(), s))
                .collect(Collectors.collectingAndThen(Collectors.toList(), create::batchInsert))
                .execute();

        // fetch emails instead of passing the previously created ones to get the NotificationReceiverEmailRecord id
        return NotificationTransformer.toProto(notificationRecord, fetchEmailsForNotification(notificationRecord));
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
     * Updates a notification.
     *
     * @param id           ID of the notification
     * @param notification new notification contents
     * @return the updated notification
     * @throws IllegalArgumentException if the Notification's email is invalid
     * @throws NotFoundException        if the Notifications could not be found in the database
     */
    public Notification updateNotification(int id, Notification notification) {
        NotificationRecord notificationRecord = create
                .fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .orElseThrow(() -> new NotFoundException("Notification does not exist!"));

        notification.getEmailsList().forEach(email -> {
            if (!EmailValidator.getInstance(false).isValid(email)) {
                throw new IllegalArgumentException(String.format("Invalid email: %s", email));
            }
        });

        NotificationTransformer.mergeRecord(notificationRecord, notification);
        notificationRecord.update();

        if (!notification.getEmailsList().isEmpty()) {
            List<NotificationReceiverEmailRecord> toInsert = notification.getEmailsList().stream()
                    .map(mail -> {
                        NotificationReceiverEmailRecord receiver = new NotificationReceiverEmailRecord();
                        receiver.setNotification(notificationRecord.getIdNotification());
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

        return this.getNotification(notificationRecord.getIdNotification()).orElseThrow(NotFoundException::new);
    }

    /**
     * Runs a sql-query in read-only mode
     *
     * @param sql the sql to execute in read-only mode
     * @return the results of the query as a stream
     */
    public Stream<Record> runReadOnlySQL(String sql) {
        return readOnlyCreate.fetchStream(sql);
    }


    /**
     * This method will check if the passed NotificationTokenRecords are already stored in the database.
     * It does so by comparing the resultId of each record for the specified notification.
     * If there are new NotificationTokenRecords, they will be stored to database.
     *
     * @param newTokenRecords the new records from a current fetch query mapped to the NOTIFICATION_TOKEN.RESULT_ID
     * @param notificationId  the id of the notification
     * @return a list of newly added NotificationTokenRecords, that were not stored in database before
     */
    public List<NotificationTokenRecord> diffTokenRecords(Map<Integer, NotificationTokenRecord> newTokenRecords, int notificationId) {
        if (newTokenRecords != null && !newTokenRecords.isEmpty()) {
            List<Query> deletionQueries = new ArrayList<>();

            try (Stream<NotificationTokenRecord> recordStream = create.selectFrom(NOTIFICATION_TOKEN)
                    .where(NOTIFICATION_TOKEN.NOTIFICATION.eq(notificationId))
                    .fetchLazy().stream()) {
                recordStream.forEach(storedRecord -> {
                    int storedTokenId = storedRecord.getResultId();
                    if (newTokenRecords.containsValue(storedTokenId)) {
                        // condition still holds true. keep token in db, but remove from new tokens
                        newTokenRecords.remove(storedTokenId);
                    } else {
                        // condition defined by query isn't true anymore, so we can delete the old token.
                        deletionQueries.add(create.delete(NOTIFICATION_TOKEN)
                                .where(NOTIFICATION_TOKEN.ID_NOTIFICATION_TOKEN.eq(storedRecord.getIdNotificationToken())));
                    }
                });
            }

            create.batch(deletionQueries).execute();

            // all remaining tokens in the map are new and have to be added to the database
            if (!newTokenRecords.isEmpty()) {
                ArrayList<NotificationTokenRecord> tokenRecordList = new ArrayList<>(newTokenRecords.values());
                create.batchInsert(tokenRecordList).execute();
                return tokenRecordList;
            }
        }
        return Collections.emptyList();
    }
}
