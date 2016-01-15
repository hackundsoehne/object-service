package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.LinkedList;

/**
 * Describes an interface to fetch mails from a mailbox
 * @author felix
 */
public interface MailFetcher {

    /**
     * Fetches Mails, that are younger than a certain amount of days.
     * @param ageOfOldestMail The maximal age of a mail, that gets fetched in days
     * @return Returns a list of fetched mails
     */
    public Message[] fetchNewSince(int ageOfOldestMail) throws UndefinedForPurposeException, MessagingException;
}
