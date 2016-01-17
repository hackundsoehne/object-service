package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Fetch mails from a mailbox.
 *
 * @author Felix Rittler
 * @author Niklas Keller
 */
public interface MailFetcher {
    /**
     * Fetches all unseen mails.
     *
     * @return Fetched mails.
     */
    Message[] fetchUnseen(String name) throws MessagingException;

    /**
     * Fetches all mails in a folder.
     *
     * @return Fetched mails.
     */
    Message[] fetchFolder(String name) throws MessagingException;
}
