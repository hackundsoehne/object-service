package edu.kit.ipd.crowdcontrol.objectservice.notification;

/**
 * @author Simon Korz
 * @version 1.0
 */
public abstract class NotificationPolicy {
    public void invoke(Notification notification) {
        if (notification.thresholdPassed()) {
            Object token = check(notification);
            if (token != null) {
                send(notification, token);
            }
        }
    }

    /**
     * Checks the query of a notification
     *
     * @param notification the notification to check
     * @return instance of an Object e.g. a result of the check
     */
    protected abstract Object check(Notification notification);

    /**
     * Sends a notification
     *
     * @param notification the notification to send
     * @param token        a token acquired from a check. Can be null.
     */
    protected abstract void send(Notification notification, Object token);
}
