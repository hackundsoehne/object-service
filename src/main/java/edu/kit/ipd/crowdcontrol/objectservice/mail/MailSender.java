package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

/**
 * Describes an interface to send mails from certain mail address to a mail address.
 *
 * @author Felix Rittler
 */

public interface MailSender {

    /**
     * Sends mails to another mail address.
     *
     * @param recipientMail the mail address, the mail gets sent
     * @param subject       the subject of the mail
     * @param message       the content of the mail
     */
    void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException;
}
