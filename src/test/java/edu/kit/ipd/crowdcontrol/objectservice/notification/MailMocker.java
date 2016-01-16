package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.mail.UndefinedForPurposeException;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class MailMocker implements MailSender{
    String subject;
    String message;
    String recipientMail;

    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException, UndefinedForPurposeException {
        this.recipientMail = recipientMail;
        this.subject = subject;
        this.message = message;


    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getRecipientMail() {
        return recipientMail;
    }
}
