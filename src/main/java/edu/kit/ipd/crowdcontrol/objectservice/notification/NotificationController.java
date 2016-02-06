package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;

import java.util.HashMap;
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
    private final NotificationOperations operations;
    private final NotificationPolicy policy;

    public NotificationController(NotificationOperations operations, NotificationPolicy policy) {
        this.operations = operations;
        this.policy = policy;

        handleMap = new HashMap<>();


        EventManager.NOTIFICATION_CREATE.getObservable().subscribe(notificationEvent -> createNotification(notificationEvent.getData()));
        EventManager.NOTIFICATION_UPDATE.getObservable().subscribe(changeEvent -> updateNotification(changeEvent.getData()));
        EventManager.NOTIFICATION_DELETE.getObservable().subscribe(notificationEvent -> deleteNotification(notificationEvent.getData()));
    }

    /**
     * Initializes the NotificationController.
     * This loads stored notifications from the database.
     */
    public void init() {
        loadNotificationsFromDatabase();
    }

    private void loadNotificationsFromDatabase() {
        operations.getAllNotifications().forEach(this::createNotification);
    }

    /**
     * Creates a new Notification from protobuf and adds it to the scheduler
     *
     * @param notificationProto the notification to create
     */
    public void createNotification(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto) {
        Notification notification = Notification.fromProtobuf(notificationProto, policy);
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
        handleMap.put(notification.getId(), notificationHandle);
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
        deleteNotification(notification.getId());
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
