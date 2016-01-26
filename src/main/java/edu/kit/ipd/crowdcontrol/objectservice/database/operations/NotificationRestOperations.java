package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.NOTIFICATION;

public class NotificationRestOperations extends AbstractOperations {
    /**
     * @param create
     *         context to use to communicate with the database
     */
    public NotificationRestOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of notifications starting from {@code cursor}.
     *
     * @param cursor
     *         Pagination cursor.
     * @param next
     *         {@code true} for next, {@code false} for previous.
     * @param limit
     *         Number of records.
     *
     * @return List of notifications.
     */
    public Range<Notification, Integer> all(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(NOTIFICATION), NOTIFICATION.ID_NOTIFICATION, cursor, next, limit)
                .map(this::toProto);
    }

    /**
     * Returns a single notification.
     *
     * @param id
     *         ID of the notification.
     *
     * @return The notification.
     */
    public Optional<Notification> get(int id) {
        return create.fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .map(this::toProto);
    }

    /**
     * Creates a new notification.
     *
     * @param toStore
     *         Notification to save.
     *
     * @return Notification with ID assigned.
     */
    public Notification create(Notification toStore) {
        if (!hasField(toStore, Notification.NAME_FIELD_NUMBER)
                || !hasField(toStore, Notification.DESCRIPTION_FIELD_NUMBER)
                || !hasField(toStore, Notification.QUERY_FIELD_NUMBER)
                || !hasField(toStore, Notification.CHECK_PERIOD_FIELD_NUMBER)
                || !hasField(toStore, Notification.SEND_THRESHOLD_FIELD_NUMBER)) {
            throw new BadRequestException("All parameters must be set!");
        }

        NotificationRecord record = mergeRecord(create.newRecord(NOTIFICATION), toStore);
        record.store();

        return toProto(record);
    }

    /**
     * Updates a notification.
     *
     * @param id
     *         ID of the notification.
     * @param notification
     *         New notification contents.
     *
     * @return Updated notification.
     */
    public Notification update(int id, Notification notification) {
        NotificationRecord record = create
                .fetchOptional(NOTIFICATION, NOTIFICATION.ID_NOTIFICATION.eq(id))
                .orElseThrow(() -> new NotFoundException("Notification does not exist!"));

        record = mergeRecord(record, notification);
        record.update();

        return toProto(record);
    }

    /**
     * Deletes a notification.
     *
     * @param id
     *         ID of the notification.
     *
     * @return {@code true} if deleted, {@code false} otherwise.
     */
    public boolean delete(int id) {
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

    private NotificationRecord mergeRecord(NotificationRecord target, Notification notification) {
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