package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;

/**
 * Transforms notification protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class NotificationTransformer extends AbstractTransformer {
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
        return merge(target, notification, (field, record) -> {
            switch (field) {
                case Notification.NAME_FIELD_NUMBER: record.setName(notification.getName());
                    break;
                case Notification.DESCRIPTION_FIELD_NUMBER: record.setDescription(notification.getDescription());
                    break;
                case Notification.QUERY_FIELD_NUMBER: record.setQuery(notification.getQuery());
                    break;
                case Notification.CHECK_PERIOD_FIELD_NUMBER: record.setCheckperiod(notification.getCheckPeriod());
                    break;
            }
        });
    }
}
