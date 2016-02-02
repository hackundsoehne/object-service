package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;

/**
 * NotificationPolicy encapsulates strategies to check and send notifications.
 *
 * @author Simon Korz
 * @version 1.0
 */
public abstract class NotificationPolicy<T> {
    private NotificationOperations operations;

    public void invoke(Notification notification) {
            T token = check(notification);
            if (token != null) {
                send(notification, token);

                //TODO handle error
                operations.deleteNotification(notification.getID());
                // will cause the notification to be removed from the scheduler
                EventManager.NOTIFICATION_DELETE.emit(notification.toProtobuf());
            }
    }

    public NotificationPolicy(NotificationOperations operations) {
        this.operations = operations;
    }

    /**
     * Checks the query of a notification.
     *
     * @param notification the notification to check
     * @return instance of a generic result as token if the check was positive, else null
     */
    protected abstract T check(Notification notification);

    /**
     * Sends a notification.
     *
     * @param notification the notification to send
     * @param token        a token acquired from a check that can be null
     * @throws NotificationNotSentException if a notification could not be sent
     */
    protected abstract void send(Notification notification, T token);
}
