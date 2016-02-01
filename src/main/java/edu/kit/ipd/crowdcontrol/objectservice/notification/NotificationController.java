package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The NotificationController holds all notifications and schedules them.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class NotificationController {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private HashMap<Integer, ScheduledFuture<?>> handleMap;
    private NotificationOperations operations;
    private NotificationPolicy policy;

    public NotificationController(NotificationOperations operations, NotificationPolicy policy) {
        this.operations = operations;
        this.policy = policy;

        handleMap = new HashMap<>();

        loadNotificationsFromDatabase();

        EventManager.NOTIFICATION_CREATE.getObservable().subscribe(this::createNotification);
        EventManager.NOTIFICATION_DELETE.getObservable().subscribe(this::deleteNotification);
        EventManager.NOTIFICATION_UPDATE.getObservable().subscribe(this::updateNotification);
    }

    private void loadNotificationsFromDatabase() {
        List<NotificationRecord> notificationList = operations.getAllNotifications();
        for (NotificationRecord record : notificationList) {
            createNotification(new Notification(record.getIdNotification(), record.getName(), record.getDescription(),
                    record.getSendthreshold(), record.getCheckperiod(), record.getQuery(), policy));
        }
    }

    /**
     * Creates a new Notification from protobuf and adds it to the scheduler
     *
     * @param notificationProto the notification to create
     */
    public void createNotification(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto) {
        Notification notification = new Notification(notificationProto.getId(), notificationProto.getName(),
                notificationProto.getDescription(), notificationProto.getSendThreshold(),
                notificationProto.getCheckPeriod(), notificationProto.getQuery(), policy);

        createNotification(notification);
    }

    /**
     * Creates a new Notification and adds it to the scheduler
     *
     * @param notification the notification to create
     */
    public void createNotification(Notification notification) {
        final ScheduledFuture<?> notificationHandle =
                scheduler.scheduleAtFixedRate(notification, 0, notification.getCheckPeriod(), TimeUnit.SECONDS);
        handleMap.put(notification.getID(), notificationHandle);
    }

    /**
     * Deletes a notificationProto
     *
     * @param notificationProto the notification to delete
     * @throws IllegalArgumentException if the given notification does not exist inside the module
     */
    public void deleteNotification(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto) {
        deleteNotification(notificationProto.getId());
    }

    /**
     * Deletes a notification
     *
     * @param notification the notification to delete
     * @throws IllegalArgumentException if the given notification does not exist inside the module
     */
    public void deleteNotification(Notification notification) {
        deleteNotification(notification.getID());
    }

    private void deleteNotification(int id) {
        if (handleMap.containsKey(id)) {
            ScheduledFuture<?> notificationHandle = handleMap.get(id);
            notificationHandle.cancel(false);
        } else {
            throw new IllegalArgumentException("The notification with ID=" + Integer.toString(id) +
                    " does not exist (is not scheduled)!");
        }
    }

    /**
     * Updates a notification
     *
     * @param notificationChangeEvent the notification change event
     */
    public void updateNotification(ChangeEvent<edu.kit.ipd.crowdcontrol.objectservice.proto.Notification> notificationChangeEvent) {
        edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto = notificationChangeEvent.getNeww();
        deleteNotification(notificationProto);
        createNotification(notificationProto);
    }
}
