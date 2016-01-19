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
     * Fetches all unseen mails in a certain folder.
     *
     * @param name the name of the folder
     * @return fetched mails
     */
    Message[] fetchUnseen(String name) throws MessagingException;

    /**
     * Fetches all mails in a folder.
     *
     * @param name the name of the folder
     * @return fetched mails
     */
    Message[] fetchFolder(String name) throws MessagingException;
}
