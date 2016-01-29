package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.SubjectTerm;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * A Mailhandler, that can send and fetch mails to another mail address/from a mailbox
 *
 * @author Felix Rittler
 * @author Niklas Keller
 */
public class MailHandler implements MailFetcher, MailSender {

    private Session session;
    private String sender;
    private Store store;

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
        session = Session.getInstance(props, auth);
        store = session.getStore();
    }

    @Override
    public Message[] fetchUnseen(String name) throws MessagingException {
        connectToStore();

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);

        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message[] messages = folder.search(unseenFlagTerm);

        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.SEEN, true);
        }

        folder.close(true);

        folder.open(Folder.READ_ONLY);

        return messages;
    }

    @Override
    public Message[] fetchFolder(String name) throws MessagingException {
        connectToStore();

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();

        Flags seen = new Flags(Flags.Flag.SEEN);
        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.SEEN, true);
        }
        folder.close(true);

        folder.open(Folder.READ_ONLY);

        return messages;
    }

    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException {
        connectToStore();

        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientMail));
        msg.setFrom(new InternetAddress(sender, sender));
        msg.setSubject(subject);
        msg.setText(message);
        msg.saveChanges();
        msg.setContent(message, "text/html; charset=utf-8");

        Transport.send(msg);
    }

    @Override
    public void markAsUnseen(Message message) throws MessagingException {
        connectToStore();

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
        if(wasOpen) {
            folder.open(mode);
        }
    }

    @Override
    public void deleteMails(Message message) throws MessagingException {
        connectToStore();

        Folder folder = message.getFolder();
        boolean wasOpen = false;
        int mode = 0;
        if (folder.isOpen()) {
            wasOpen = true;
            mode = folder.getMode();
            folder.close(true);
        }
        folder.open(Folder.READ_WRITE);

        message.setFlag(Flags.Flag.DELETED, false);

        folder.close(true);
        if(wasOpen) {
            folder.open(mode);
        }
    }

    private void connectToStore() throws MessagingException {
        if (!store.isConnected()) {
            store.connect();
        }
    }
}