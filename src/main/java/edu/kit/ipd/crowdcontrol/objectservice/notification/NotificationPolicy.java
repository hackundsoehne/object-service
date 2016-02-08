package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;

import java.util.Collection;

/**
 * NotificationPolicy encapsulates strategies to check and send notifications.
 *
 * @author Simon Korz
 * @version 1.0
 */

public abstract class NotificationPolicy<T extends Collection<?>> {
    private final NotificationOperations operations;

    /**
     * Checks a notification's condition and sends the notification if the condition is met
     *
     * @param notification the notification to check and send
     */
    public void invoke(Notification notification) {
            T tokens = check(notification);
            if (tokens != null) {
                send(notification, tokens);

                if (notification.isSendOnce()) {
                    operations.deleteNotification(notification.getId());
                    // will cause the notification to be removed from the scheduler
                    EventManager.NOTIFICATION_DELETE.emit(notification.toProtobuf());
                }
            }
    }

    public NotificationPolicy(NotificationOperations operations) {
        this.operations = operations;
    }

    /**
     * Checks the query of a notification.
     * Note that an empty collection returned, still is an indicator for a positive check.
     *
     * @param notification the notification to check
     * @return instance of a generic collection of tokens if the check was positive, else null
     *
     */
    protected abstract T check(Notification notification);

    /**
     * Sends a notification.
     *
     * @param notification the notification to send
     * @param tokens        a list of tokens, can be empty
     * @throws NotificationNotSentException if a notification could not be sent
     */
    protected abstract void send(Notification notification, T tokens);

}
