package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperation;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.mail.UndefinedForPurposeException;
import org.jooq.Record;
import org.jooq.Result;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class DBEmailNotificationPolicy extends NotificationPolicy<Result<Record>> {
    private MailSender mailSender;
    private String receiver;
    private NotificationOperation operation;


    public DBEmailNotificationPolicy(MailSender mailSender, String receiver, NotificationOperation operation) {
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
        if (token != null) {
            message.append(token.formatHTML());
        }

        try {
            mailSender.sendMail(receiver, notification.getDescription(), message.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UndefinedForPurposeException e) {
            e.printStackTrace();
        }

        // if sent update lastSent
        Instant now = Instant.now();
        notification.setLastSent(now);
        operation.updateLastSentForNotification(notification.getID(), now);
    }

}
