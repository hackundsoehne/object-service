package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperation;

import java.util.HashMap;
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

        handleMap = new HashMap<Integer, ScheduledFuture<?>>();
    }

    public void newNotification(Notification notification) {
        // TODO real event handling
        final ScheduledFuture<?> notificationHandle =
                scheduler.scheduleAtFixedRate(notification, 0, notification.getCheckPeriod(), TimeUnit.SECONDS);
        handleMap.put(notification.getID(), notificationHandle);
    }

    public void deleteNotification(Notification notification) {
        ScheduledFuture<?> notificationHandle = handleMap.get(notification.getID());
        notificationHandle.cancel(true);
    }
}
