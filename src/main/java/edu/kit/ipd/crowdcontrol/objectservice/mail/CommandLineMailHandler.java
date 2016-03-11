package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * A Mailhandler that prints to the command-line, used for running the Object-Service in an development-environment.
 * @author LeanderK
 * @version 1.0
 */
public class CommandLineMailHandler implements MailSender, MailFetcher {
    private static final Logger LOGGER = LogManager.getRootLogger();

    /**
     * Fetches all unseen mails in a certain folder and marks them as seen.
     *
     * @param name the name of the folder
     * @return fetched mails
     */
    @Override
    public Message[] fetchUnseen(String name) throws MessagingException {
        LOGGER.debug("call to fetchUnseen");
        return new Message[0];
    }

    /**
     * Fetches all mails in a folder and marks them as seen.
     *
     * @param name the name of the folder
     * @return fetched mails
     */
    @Override
    public Message[] fetchFolder(String name) throws MessagingException {
        LOGGER.debug("call to fetchUnseen");
        return new Message[0];
    }

    /**
     * Marks a message in a certain folder as unseen.
     *
     * @param message the message to mark
     * @throws MessagingException throws a MessagingException, if there are any problems with the message
     */
    @Override
    public void markAsUnseen(Message message) throws MessagingException {
        LOGGER.debug("call to markAsUnseen with message: {}", message);
    }

    /**
     * Deletes a message from the folder.
     *
     * @param message the message to delete
     * @throws MessagingException throws a MessagingException, if there are any problems with the message
     */
    @Override
    public void deleteMails(Message message) throws MessagingException {
        LOGGER.debug("call to deleteMails with message: {}", message);
    }

    /**
     * Sends mails to another mail address.
     *
     * @param recipientMail the mail address, the mail gets sent
     * @param subject       the subject of the mail
     * @param message       the content of the mail
     */
    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException {
        LOGGER.info("call to sendMail, parameters recipientMail : {}, subject : {}, message: {}",
                recipientMail, subject, message);
    }
}
