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
     * @param recipientMail The mail address, the mail gets sent
     * @param subject       The subject of the mail
     * @param message       The content of the mail
     */
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException, UndefinedForPurposeException;
}
