package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import java.io.UnsupportedEncodingException;

/**
 * A mail handler that prints to the command-line, used for running the Object-Service in an development-environment.
 * @author LeanderK
 * @version 1.0
 */
public class CommandLineMailHandler implements MailSender, MailFetcher {
    private static final Logger LOGGER = LogManager.getLogger(CommandLineMailHandler.class);


    /**
     * {@inheritDoc}
     */
    @Override
    public Message[] fetchUnseen(String name) throws MessagingException {
        LOGGER.debug("call to fetchUnseen");
        return new Message[0];
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Message[] fetchFolder(String name) throws MessagingException {
        LOGGER.debug("call to fetchUnseen");
        return new Message[0];
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsUnseen(Message message) throws MessagingException {
        LOGGER.debug("call to markAsUnseen with message: {}", message);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMails(Message message) throws MessagingException {
        LOGGER.debug("call to deleteMails with message: {}", message);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Message[] fetchUnseen() throws MessagingException {
        return new Message[0];
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException {
        LOGGER.info("call to sendMail, parameters recipientMail : {}, subject : {}, message: {}",
                recipientMail, subject, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close(Folder folder) {
        LOGGER.info("call to close with folder: {}", folder);
    }
}
