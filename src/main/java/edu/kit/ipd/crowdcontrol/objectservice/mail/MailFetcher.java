package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.Folder;
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
     * Get all unseen mail from the default location and marks them as seen.
     * @return A array of unseen mails
     */
    Message[] fetchUnseen() throws MessagingException;

    /**
     * Closes a folder and its resources.
     * @param folder the folder to close
     * @throws MessagingException in case of problems with closing
     */
    void close(Folder folder) throws MessagingException;
}
