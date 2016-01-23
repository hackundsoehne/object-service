package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION;

public class NotificationRestOperations extends AbstractOperations {
    /**
     * @param create context to use to communicate with the database
     */
    public NotificationRestOperations(DSLContext create) {
        super(create);
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
        return getNextRange(create.selectFrom(NOTIFICATION), NOTIFICATION.ID_NOTIFICATION, cursor, next, limit)
                .map(this::toProto);
    }

    /**
     * Returns a single notification.
     *
     * @param id the ID of the notification
     * @return the notification of empty if not found
     */
    public Optional<Notification> getNotification(int id) {
        return create.fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .map(this::toProto);
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

        NotificationRecord record = toRecord(toStore);
        record.store();

        return toProto(record);
    }

    /**
     * Updates a notification.
     *
     * @param id ID of the notification
     * @param notification new notification contents
     * @return the updated notification
     */
    public Notification updateNotification(int id, Notification notification) {
        NotificationRecord notificationRecord = toRecord(notification);
        notificationRecord.setIdNotification(id);
        return create.update(NOTIFICATION)
                .set(notificationRecord)
                .where(NOTIFICATION.ID_NOTIFICATION.eq(id))
                .returning()
                .fetchOptional()
                .map(this::toProto)
                .orElseThrow(() -> new NotFoundException("Notification does not exist!"));
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

    private Notification toProto(NotificationRecord record) {
        return Notification.newBuilder()
                .setId(record.getIdNotification())
                .setName(record.getName())
                .setDescription(record.getDescription())
                .setQuery(record.getQuery())
                .setSendThreshold(record.getSendthreshold())
                .setCheckPeriod(record.getCheckperiod())
                .build();
    }

    private NotificationRecord toRecord(Notification notification) {
        NotificationRecord target = create.newRecord(NOTIFICATION);
        if (notification.hasField(notification.getDescriptorForType().findFieldByNumber(Notification.NAME_FIELD_NUMBER))) {
            target.setName(notification.getName());
        }

        if (notification.hasField(notification.getDescriptorForType().findFieldByNumber(Notification.DESCRIPTION_FIELD_NUMBER))) {
            target.setDescription(notification.getDescription());
        }

        if (notification.hasField(notification.getDescriptorForType().findFieldByNumber(Notification.QUERY_FIELD_NUMBER))) {
            target.setQuery(notification.getQuery());
        }

        if (notification.hasField(notification.getDescriptorForType().findFieldByNumber(Notification.SEND_THRESHOLD_FIELD_NUMBER))) {
            target.setSendthreshold(notification.getSendThreshold());
        }

        if (notification.hasField(notification.getDescriptorForType().findFieldByNumber(Notification.CHECK_PERIOD_FIELD_NUMBER))) {
            target.setCheckperiod(notification.getCheckPeriod());
        }

        return target;
    }
}