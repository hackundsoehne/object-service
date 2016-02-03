package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationReceiverEmailRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms notification protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class NotificationTransformer extends AbstractTransformer {
    /**
     * Converts a notification notificationRecord to its protobuf representation.
     *
     * @param notificationRecord notification notificationRecord
     * @return Notification.
     */
    public static Notification toProto(NotificationRecord notificationRecord, List<NotificationReceiverEmailRecord> emailRecordList) {
        Notification.Builder builder = Notification.newBuilder()
                .setId(notificationRecord.getIdNotification())
                .setName(notificationRecord.getName())
                .setDescription(notificationRecord.getDescription())
                .setQuery(notificationRecord.getQuery())
                .setCheckPeriod(notificationRecord.getCheckperiod())
                .setSendOnce(notificationRecord.getSendOnce())
                .addAllEmails(emailRecordList.stream().map(NotificationReceiverEmailRecord::getEmail)
                        .collect(Collectors.toList()));
        return builder.build();
    }

    /**
     * Merges a record with the set properties of a protobuf notification.
     *
     * @param target       record to merge into
     * @param notification message to merge from
     * @return Merged notification record.
     */
    public static NotificationRecord mergeRecord(NotificationRecord target, Notification notification) {
        return merge(target, notification, (field, record) -> {
            switch (field) {
                case Notification.NAME_FIELD_NUMBER:
                    record.setName(notification.getName());
                    break;
                case Notification.DESCRIPTION_FIELD_NUMBER:
                    record.setDescription(notification.getDescription());
                    break;
                case Notification.QUERY_FIELD_NUMBER:
                    record.setQuery(notification.getQuery());
                    break;
                case Notification.CHECK_PERIOD_FIELD_NUMBER:
                    record.setCheckperiod(notification.getCheckPeriod());
                    break;
                case Notification.SEND_ONCE_FIELD_NUMBER:
                    record.setSendOnce(notification.getSendOnce());
                    break;
            }
        });
    }
}
