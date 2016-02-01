package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.Record;
import org.jooq.Result;

import java.time.Instant;

/**
 * The SQLEmailNotificationPolicy checks given queries in a SQL-Database and sends notifications via email.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class SQLEmailNotificationPolicy extends NotificationPolicy<Result<Record>> {
    private MailSender mailSender;
    private String receiver;
    private NotificationOperations operation;


    /**
     * @param mailSender an implementation of the MailSender interface
     * @param receiver   email address of the receiver
     * @param operation  instance of the notification operations
     */
    public SQLEmailNotificationPolicy(MailSender mailSender, String receiver, NotificationOperations operation) {
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
        int count = token.size() <= 10 ? token.size() : 10;
        for (int i = 0; i < count; i++) {
            message.append(token.get(i).toString());
        }

        String subject = "[CrowdControl Notification] " + notification.getName();
        try {
            mailSender.sendMail(receiver, subject, message.toString());
        } catch (Exception e) {
            throw new NotificationNotSentException(e);
        }

        // update lastSent
        Instant now = Instant.now();
        notification.setLastSent(now);
        operation.updateLastSentForNotification(notification.getID(), now);
    }
}

