package edu.kit.ipd.crowdcontrol.objectservice.notification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * The Notification class represents a complete Notification.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class Notification implements Runnable {
    private int ID;
    private String name;
    private String description;
    private long sendThreshold;
    private Instant lastSent;
    private long checkPeriod;
    private String query;
    private NotificationPolicy policy;

    /**
     * Creates a new instance of Notification.
     *
     * @param name          the name of the notification
     * @param description   the description of the notification
     * @param checkPeriod   the period of time in seconds that has to pass before the next check
     * @param sendThreshold the time in seconds that has to pass before a notification is sent again
     * @param query         the query which defines a constraint to check
     * @param policy        the policy to check and send a notification
     */
    public Notification(int ID, String name, String description, long sendThreshold, long checkPeriod, String query, NotificationPolicy policy) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.sendThreshold = sendThreshold;
        this.checkPeriod = checkPeriod;
        this.query = query;
        this.policy = policy;

        // set last sent in past to allow immediate checking after creation
        setLastSent(Instant.now().minus(sendThreshold, ChronoUnit.SECONDS));
    }

    /**
     * Invokes checking and sending a notification
     */
    public void run() {
        policy.invoke(this);
    }

    /**
     * @return true if threshold since last sent has passed, else false
     */
    public boolean thresholdPassed() {
        return ((lastSent.plusSeconds(sendThreshold)
                .compareTo(Instant.now())) <= 0);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Sets the lastSent attribute
     *
     * @param lastSent the time when the notification was last sent
     */
    public void setLastSent(Instant lastSent) {
        this.lastSent = lastSent;
    }

    public long getCheckPeriod() {
        return checkPeriod;
    }
}
