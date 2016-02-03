package edu.kit.ipd.crowdcontrol.objectservice.notification;

import java.util.ArrayList;

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
    private int checkPeriod;
    private String query;
    private boolean sendOnce = false;
    private ArrayList<String> receiverEmails;
    private NotificationPolicy policy;

    /**
     * Creates a new instance of Notification.
     *
     * @param ID             the id of the notification
     * @param name           the name of the notification
     * @param description    the description of the notification
     * @param checkPeriod    the period of time in seconds that has to pass before the next check
     * @param query          the query which defines a constraint to check
     * @param sendOnce       true if the notification has to be sent only once
     * @param receiverEmails email addresses the notification will be sent to
     * @param policy         the policy to check and send a notification
     */
    public Notification(int ID, String name, String description, int checkPeriod, String query, boolean sendOnce,
                        ArrayList<String> receiverEmails, NotificationPolicy policy) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.checkPeriod = checkPeriod;
        this.query = query;
        this.sendOnce = sendOnce;
        this.receiverEmails = receiverEmails;
        this.policy = policy;
    }

    /**
     * Creates a Notification from protobuf
     *
     * @param notificationProto the notification protobuf class
     * @param policy            the notification policy
     */
    public Notification(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto, NotificationPolicy policy) {
        this(notificationProto.getId(), notificationProto.getName(), notificationProto.getDescription(),
                notificationProto.getCheckPeriod(), notificationProto.getQuery(), notificationProto.getSendOnce(),
                new ArrayList<>(notificationProto.getEmailsList()), policy);
    }

    /**
     * Creates a new notification from protobuf
     *
     * @param notificationProto the notification protobuf
     * @param policy            the notification policy
     * @return the new notification
     */
    public static Notification fromProtobuf(edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto, NotificationPolicy policy) {
        return new Notification(notificationProto, policy);
    }

    /**
     * Create a new protobuf from this notification
     *
     * @return the protobuf representation of this notification
     */
    public edu.kit.ipd.crowdcontrol.objectservice.proto.Notification toProtobuf() {
        edu.kit.ipd.crowdcontrol.objectservice.proto.Notification.Builder builder =
                edu.kit.ipd.crowdcontrol.objectservice.proto.Notification.newBuilder()
                        .setId(getID())
                        .setName(getName())
                        .setDescription(getDescription())
                        .setCheckPeriod(getCheckPeriod())
                        .setQuery(getQuery())
                        .setSendOnce(isSendOnce())
                        .addAllEmails(getReceiverEmails());
        return builder.buildPartial();
    }

    /**
     * Invokes checking and sending a notification
     */
    public void run() {
        policy.invoke(this);
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
     * @return the checkPeriod
     */
    public int getCheckPeriod() {
        return checkPeriod;
    }

    /**
     * @return the emails of the receivers
     */
    public ArrayList<String> getReceiverEmails() {
        return receiverEmails;
    }

    /**
     * @return true if notification has to be sent only once
     */
    public boolean isSendOnce() {
        return sendOnce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        if (ID != that.ID) return false;
        if (checkPeriod != that.checkPeriod) return false;
        if (sendOnce != that.sendOnce) return false;
        if (!name.equals(that.name)) return false;
        if (!description.equals(that.description)) return false;
        if (!query.equals(that.query)) return false;
        if (!receiverEmails.equals(that.receiverEmails)) return false;
        return policy.equals(that.policy);

    }

    @Override
    public int hashCode() {
        int result = ID;
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + checkPeriod;
        result = 31 * result + query.hashCode();
        result = 31 * result + (sendOnce ? 1 : 0);
        result = 31 * result + receiverEmails.hashCode();
        result = 31 * result + policy.hashCode();
        return result;
    }
}
