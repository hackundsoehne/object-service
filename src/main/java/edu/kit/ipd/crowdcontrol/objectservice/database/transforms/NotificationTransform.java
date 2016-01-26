package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;

/**
 * Transforms notification protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class NotificationTransform {
    /**
     * Converts a notification record to its protobuf representation.
     *
     * @param record notification record
     *
     * @return Notification.
     */
    public static Notification toProto(NotificationRecord record) {
        return Notification.newBuilder()
                .setId(record.getIdNotification())
                .setName(record.getName())
                .setDescription(record.getDescription())
                .setQuery(record.getQuery())
                .setSendThreshold(record.getSendthreshold())
                .setCheckPeriod(record.getCheckperiod())
                .build();
    }

    /**
     * Merges a record with the set properties of a protobuf notification.
     *
     * @param target record to merge into
     * @param notification message to merge from
     *
     * @return Merged notification record.
     */
    public static NotificationRecord mergeRecord(NotificationRecord target, Notification notification) {
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
