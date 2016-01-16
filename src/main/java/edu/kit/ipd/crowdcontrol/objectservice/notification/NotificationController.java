package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperation;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class NotificationController {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private HashMap<Integer, ScheduledFuture<?>> handleMap;
    private NotificationOperation operation;
    private NotificationPolicy policy;

    public NotificationController(NotificationOperation operation, NotificationPolicy policy) {
        this.operation = operation;
        this.policy = policy;

        handleMap = new HashMap<>();

        loadNotificationsFromDatabase();

        EventManager.NOTIFICATION_CREATE.getObservable().subscribe(this::newNotification);
        EventManager.NOTIFICATION_DELETE.getObservable().subscribe(this::deleteNotification);
        EventManager.NOTIFICATION_UPDATE.getObservable().subscribe(this::updateNotification);
    }

    private void loadNotificationsFromDatabase() {
        List<NotificationRecord> notificationList = operation.getAllNotifications();
        for (NotificationRecord record : notificationList) {
            newNotification(new Notification(record.getIdnotification(), record.getName(), record.getDescription(),
                    record.getSendthreshold(), record.getCheckperiod(), record.getQuery(), policy));
        }
    }

    /**
     * Creates a new Notification
     *
     * @param notification the notification to create
     */
    public void newNotification(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notification) {
        Notification internalNotification = new Notification(notification.getId(), notification.getName(),
                notification.getDescription(), notification.getSendThreshold(),
                notification.getCheckPeriod(), notification.getQuery(), policy);

        newNotification(internalNotification);
    }

    /**
     * Creates a new Notification
     *
     * @param notification the notification to create
     */
    public void newNotification(Notification notification) {
        final ScheduledFuture<?> notificationHandle =
                scheduler.scheduleAtFixedRate(notification, 0, notification.getCheckPeriod(), TimeUnit.SECONDS);
        handleMap.put(notification.getID(), notificationHandle);
    }

    /**
     * Deletes a notification
     *
     * @param notification the notification to delete
     */
    public void deleteNotification(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notification) {
        deleteNotification(notification.getId());
    }

    /**
     * Deletes a notification
     *
     * @param notification the notification to delete
     */
    public void deleteNotification(Notification notification) {
        deleteNotification(notification.getID());
    }

    private void deleteNotification(int id) {
        ScheduledFuture<?> notificationHandle = handleMap.get(id);
        notificationHandle.cancel(true);
    }

    /**
     * Updates a notification
     *
     * @param notificationChangeEvent the notification change event
     */
    public void updateNotification(ChangeEvent<edu.kit.ipd.crowdcontrol.objectservice.proto.Notification> notificationChangeEvent) {
        edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notification = notificationChangeEvent.getNeww();
        deleteNotification(notification);
        newNotification(notification);
    }
}
