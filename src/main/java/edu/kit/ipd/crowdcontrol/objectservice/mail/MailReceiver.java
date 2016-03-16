package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.util.Properties;

/**
 * A Mailhandler, that can send and fetch mails to another mail address/from a mailbox
 *
 * @author Felix Rittler
 * @author Niklas Keller
 * @author Marcel Hollerbach
 */
public class MailReceiver implements MailFetcher {
    public enum Protocol {
        imap, imaps,
        pop3, pop3s
    }

    private final Protocol protocol;
    private final String user;
    private final String password;
    private final String host;
    private final int port;
    private final String defaultInbox;
    private Properties props;
    private static final Logger LOGGER = LogManager.getLogger(MailReceiver.class);


    /**
     * Creates a new mail receiver, which can fetch mails from a mailbox.
     * @param protocol protocol to receive mails (e.g. imap)
     * @param user username on the mailserver
     * @param password password on the mailserver
     * @param host address of the server host
     * @param port port, the mail receiver should access the server
     */
    public MailReceiver(Protocol protocol, String user, String password, String host, int port, String defaultInbox, boolean debug) {
        this.protocol = protocol;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.defaultInbox = defaultInbox;

        props = new Properties();

        //we can always send starttls as this will ask to server if he supports it
        props.setProperty("mail.imap.starttls.enable", "true");
        props.setProperty("mail.pop3.starttls.enable", "true");

        if (debug)
          props.setProperty("mail.debug", true+"");

        props.setProperty("mail."+protocol+".ssl.checkserveridentity", "true");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message[] fetchUnseen(String name) throws MessagingException {
        LOGGER.trace("Started fetching unseen mails from folder " + name + ".");
        Store store = connect();

        Message[] messages;

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);

        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        messages = folder.search(unseenFlagTerm);

        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.SEEN, true);
        }
        folder.close(true);

        folder.open(Folder.READ_ONLY);

        LOGGER.trace("Successfully completed fetching " + messages.length + " unseen mails from folder" + name + ".");
        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message[] fetchFolder(String name) throws MessagingException {
        LOGGER.trace("Started fetching mails from folder " + name + ".");
        Store store = connect();

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();

        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.SEEN, true);
        }
        folder.close(true);

        folder.open(Folder.READ_ONLY);

        LOGGER.trace("Successfully completed fetching " + messages.length + " mails from folder" + name + ".");
        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsUnseen(Message message) throws MessagingException {
        LOGGER.trace("Started marking message with subject \"" + message.getSubject() + "\" as unseen.");
        Folder folder = message.getFolder();
        boolean wasOpen = false;
        int mode = 0;
        if (folder.isOpen()) {
            wasOpen = true;
            mode = folder.getMode();
            folder.close(true);
        }
        folder.open(Folder.READ_WRITE);

        message.setFlag(Flags.Flag.SEEN, false);

        folder.close(true);
        if (wasOpen) {
            folder.open(mode);
        }
        LOGGER.trace("Successfully completed marking message with subject \"" + message.getSubject() + "\" as unseen.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMails(Message message) throws MessagingException {
        LOGGER.trace("Started deleting message with subject \"" + message.getSubject() + "\".");

        Folder folder = message.getFolder();
        boolean wasOpen = false;
        int mode = 0;
        if (folder.isOpen()) {
            wasOpen = true;
            mode = folder.getMode();
            folder.close(true);
        }
        folder.open(Folder.READ_WRITE);

        message.setFlag(Flags.Flag.DELETED, true);

        folder.close(true);

        if (wasOpen) {
            folder.open(mode);
        }
        LOGGER.trace("Successfully completed deleting message with subject \"" + message.getSubject() + "\".");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message[] fetchUnseen() throws MessagingException {
        return fetchUnseen(defaultInbox);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close(Folder folder) throws MessagingException {
        LOGGER.trace("Started closing folder " + folder.getFullName() + ".");
            if (folder.isOpen()) {
                folder.close(true);
            }
            if (folder.getStore().isConnected()) {
                folder.getStore().close();
            }

        LOGGER.trace("Successfully completed closing folder " + folder.getFullName() + ".");
    }

    private Store connect() throws MessagingException {
        Session session = Session.getInstance(props);
        Store store = session.getStore(protocol.toString());
        store.connect(host, port, user, password);
        return store;
    }
}