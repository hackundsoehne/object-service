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
 * @author Felix Rittler
 * @author Niklas Keller
 */
public class MailHandler implements MailFetcher, MailSender {

    private Properties props;
    private Authenticator auth;
    private Session session;
    private String sender;
    private Store store;

    public MailHandler(Properties props, Authenticator auth) throws MessagingException {
        sender = props.getProperty("sender");
        props.remove("sender");
        this.auth = auth;
        this.props = props;
        session = Session.getInstance(props, auth);
        store = session.getStore();
    }

    @Override
    public Message[] fetchUnseen(String name) throws MessagingException {
        if (!store.isConnected()) {
            store.connect();
        }

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_ONLY);

        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message[] messages = folder.search(unseenFlagTerm);

        return messages;
    }

    @Override
    public Message[] fetchFolder(String name) throws MessagingException {
        if (!store.isConnected()) {
            store.connect();
        }

        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages();
        return messages;
    }

    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException, UndefinedForPurposeException {
        if (!store.isConnected()) {
            store.connect();
        }

        Message msg = new MimeMessage(session);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientMail));
        msg.setFrom(new InternetAddress(sender, sender));
        msg.setSubject(subject);
        msg.setText(message);
        msg.saveChanges();

        Transport.send(msg);
    }

    protected void deleteMails(String subject, String name) throws MessagingException{
        if (!store.isConnected()) {
            store.connect();
        }
        
        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);
        SubjectTerm subjectterm = new SubjectTerm(subject);
        Message[] messages = folder.search(subjectterm);
        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.DELETED, true);
        }
        folder.close(true);
    }
}