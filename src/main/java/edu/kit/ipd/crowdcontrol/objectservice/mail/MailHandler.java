package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * A Mailhandler, that can send and fetch mails to another mail address/from a mailbox
 *
 * @author Felix Rittler
 * @author Niklas Keller
 */
public class MailHandler implements MailFetcher, MailSender {

    private String sender;
    private Properties props;
    private Authenticator auth;


    /**
     * A Mailhandler object to send and fetch emails.
     *
     * @param props properties, that describe the connection to the mailserver
     * @param auth  an authenticator authenticating the user to connect to the account
     * @throws AuthenticationFailedException Throws this exception, if there is a problem with the authentication
     * @throws MessagingException            For other problems e.g. with properties object: unvalid domains, ports not valid etc.
     */
    public MailHandler(Properties props, Authenticator auth) throws MessagingException {
        sender = props.getProperty("sender");
        props.remove("sender");
        this.props = props;
        this.auth = auth;
    }

    @Override
    public Message[] fetchUnseen(String name) throws MessagingException {
        Store store = connect();

        Message[] messages = new Message[0];

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

        return messages;
    }

    @Override
    public Message[] fetchFolder(String name) throws MessagingException {
        Store store = connect();

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();

        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.SEEN, true);
        }
        folder.close(true);

        folder.open(Folder.READ_ONLY);
        return messages;
    }

    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException {
        Session session = Session.getInstance(props, auth);

        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientMail));
        msg.setFrom(new InternetAddress(sender, "CrowdControl"));
        msg.setContent(message, "text/html; charset=utf-8");
        msg.setSubject(subject);
        msg.setText(message);

        Transport.send(msg);
    }

    @Override
    public void markAsUnseen(Message message) throws MessagingException {
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
    }

    @Override
    public void deleteMails(Message message) throws MessagingException {
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
    }

    /**
     * Closes the folder and the store of the messages.
     * @param messages the messages their resources become closed
     * @throws MessagingException in case of problems with closing
     */
    public void close(Message[] messages) throws MessagingException {
        if (messages.length > 0) {
            if (messages[0].getFolder().isOpen()) {
                messages[0].getFolder().close(true);
            }
            if (messages[0].getFolder().getStore().isConnected()) {
                messages[0].getFolder().getStore().close();
            }
        }
    }

    private Store connect() throws MessagingException {
        Session session = Session.getInstance(props, auth);
        Store store = session.getStore();
        store.connect();
        return store;
    }
}