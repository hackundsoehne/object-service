package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperation;
import org.jooq.Record;
import org.jooq.Result;

import java.time.Instant;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class DBEmailNotificationPolicy extends NotificationPolicy {
    private MailSender mailSender;
    private String receiver;
    private NotificationOperation operation;


    public DBEmailNotificationPolicy(MailSender mailSender, String receiver, NotificationOperation operation) {
        this.mailSender = mailSender;
        this.receiver = receiver;
        this.operation = operation;
    }

    @Override
    protected Object check(Notification notification) {

        return operation.runReadOnlySQL(notification.getQuery());
    }


    @Override
    protected void send(Notification notification, Object token) {
        @SuppressWarnings("unchecked")
        Result<Record> result;
        try {
            result = (Result<Record>) token;
        } catch (ClassCastException cce) {
            result = null;
        }

        //TODO can mail format be html?
        StringBuilder message = new StringBuilder();
        message.append(notification.getDescription());
        if (result != null) {
            message.append(result.formatHTML());
        }

        //TODO send mail


        // if sent do
        Instant now = Instant.now();
        notification.setLastSent(now);
        operation.updateLastSentForNotification(notification.getID(), now);
    }

}
