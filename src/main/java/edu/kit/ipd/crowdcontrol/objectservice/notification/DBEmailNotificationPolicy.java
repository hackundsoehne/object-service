package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.Record;
import org.jooq.Result;

import java.time.Instant;

/**
 * The DBEmailNotificationPolicy
 *
 * @author Simon Korz
 * @version 1.0
 */
public class DBEmailNotificationPolicy extends NotificationPolicy<Result<Record>> {
    private MailSender mailSender;
    private String receiver;
    private NotificationOperations operation;


    public DBEmailNotificationPolicy(MailSender mailSender, String receiver, NotificationOperations operation) {
        this.mailSender = mailSender;
        this.receiver = receiver;
        this.operation = operation;
    }

    @Override
    protected Result<Record> check(Notification notification) {
        Result<Record> result = operation.runReadOnlySQL(notification.getQuery());
        if (result.isNotEmpty()) {
            return result;
        } else {
            return null;
        }
    }

    @Override

    protected void send(Notification notification, Result<Record> token) {
        StringBuilder message = new StringBuilder();
        message.append(notification.getDescription());
        message.append("\n");
        for (Record record : token) {
            message.append(record.toString());
        }

        try {
            mailSender.sendMail(receiver, notification.getName(), message.toString());
        } catch (Exception e) {
            throw new NotificationNotSentException(e);
        }

        // update lastSent
        Instant now = Instant.now();
        notification.setLastSent(now);
        operation.updateLastSentForNotification(notification.getID(), now);
    }
}

