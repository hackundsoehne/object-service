package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Boolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The Notification class represents a complete Notification.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class Notification implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(Notification.class);

    private final int id;
    private final String name;
    private final String description;
    private final int checkPeriod;
    private final String query;
    private final boolean sendOnce;
    private final List<String> receiverEmails;
    private final NotificationPolicy policy;

    /**
     * Creates a new instance of Notification.
     *
     * @param id             the id of the notification
     * @param name           the name of the notification
     * @param description    the description of the notification
     * @param checkPeriod    the period of time in seconds that has to pass before the next check
     * @param query          the query which defines a constraint to check
     * @param sendOnce       true if the notification has to be sent only once
     * @param receiverEmails email addresses the notification will be sent to
     * @param policy         the policy to check and send a notification
     */
    public Notification(int id, String name, String description, int checkPeriod, String query, boolean sendOnce,
                        List<String> receiverEmails, NotificationPolicy policy) {
        this.id = id;
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
                notificationProto.getCheckPeriod(), notificationProto.getQuery(), notificationProto.getSendOnce().getValue(),
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
                        .setId(getId())
                        .setName(getName())
                        .setDescription(getDescription())
                        .setCheckPeriod(getCheckPeriod())
                        .setQuery(getQuery())
                        .setSendOnce(Boolean.newBuilder().setValue(isSendOnce()).build())
                        .addAllEmails(getReceiverEmails());
        return builder.buildPartial();
    }

    /**
     * Invokes checking and sending a notification
     */
    public void run() {
        try {
            policy.invoke(this);
        } catch (RuntimeException e) {
            LOGGER.error("An Exception occurred while checking or sending a notification.", e);
        }
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
     * @return the id
     */
    public int getId() {
        return id;
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
    public List<String> getReceiverEmails() {
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

        if (id != that.id) return false;
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
        int result = id;
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
