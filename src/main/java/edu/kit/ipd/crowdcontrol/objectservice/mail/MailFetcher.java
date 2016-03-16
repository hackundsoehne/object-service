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
     * Fetches all unseen mails in a certain folder and marks them as seen.
     *
     * @param name the name of the folder
     * @return fetched mails
     */
    Message[] fetchUnseen(String name) throws MessagingException;

    /**
     * Fetches all mails in a folder and marks them as seen.
     *
     * @param name the name of the folder
     * @return fetched mails
     */
    Message[] fetchFolder(String name) throws MessagingException;

    /**
     * Marks a message in a certain folder as unseen.
     * @param message the message to mark
     * @throws MessagingException throws a MessagingException, if there are any problems with the message
     */
    void markAsUnseen(Message message) throws MessagingException;

    /**
     * Deletes a message from the folder.
     * @param message the message to delete
     * @throws MessagingException throws a MessagingException, if there are any problems with the message
     */
    void deleteMails(Message message) throws MessagingException;

    /**
     * Get all unseen mail from the default location
     * @return A array of unseen mails
     */
    Message[] fetchUnseen() throws MessagingException;

    /**
     * Closes the folder and the store of the messages.
     * @param messages the messages their resources become closed (have to be in the same folder)
     * @throws MessagingException in case of problems with closing
     */
    void close(Message[] messages) throws MessagingException;
}
