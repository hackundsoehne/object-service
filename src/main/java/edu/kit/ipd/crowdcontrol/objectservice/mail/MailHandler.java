package edu.kit.ipd.crowdcontrol.objectservice.mail;

import java.io.UnsupportedEncodingException;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.LinkedList;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;

/**
 * A Mailhandler, that can send and fetch mails to another mail address/from a mailbox
 */
public class MailHandler implements MailFetcher, MailSender {


    private Properties props;
    private Authenticator auth;
    private Session session;
    private String sender;
    Store store;
    Folder emailfolder;


    public MailHandler(Properties props, Authenticator auth) throws IllegalPropertiesException, MessagingException {
        sender = props.getProperty("sender");
        props.remove("sender");
        this.auth = auth;
        this.props = props;
        session = Session.getInstance(props, auth);
        try {
            store = session.getStore("imap");
        } catch (NoSuchProviderException e) {
            throw new IllegalPropertiesException();
        }
        store.connect();
        emailfolder = store.getFolder("INBOX");
        emailfolder.open(Folder.READ_ONLY);
    }

    /**
     * Fetches Mails, that are younger than a certain amount of days.
     *
     * @param ageOfOldestMail The maximal age of a mail, that gets fetched in days
     * @return Returns a list of fetched mails
     */
    @Override
    public Message[] fetchNewSince(int ageOfOldestMail) throws UndefinedForPurposeException, MessagingException {
        Message[] messages = emailfolder.getMessages();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, ageOfOldestMail * -1);
        Date dateOfOldestMail = cal.getTime();
        LinkedList<Message> mails = new LinkedList<Message>();
        System.out.print(messages.length);
        for (int i = 0; i < messages.length /*&& messages[i].getReceivedDate().before(dateOfOldestMail)*/; i++) {
            mails.add(messages[i]);
        }
        return messages;
    }

    /**
     * Sends mails to another mail address.
     *
     * @param recipientMail The mail address, the mail gets sent
     * @param subject       The subject of the mail
     * @param message       The content of the mail
     */
    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException, UndefinedForPurposeException {
        Message msg = new MimeMessage(session);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientMail));
        msg.setFrom(new InternetAddress(sender, sender));
        msg.setSubject(subject);
        msg.setText(message);
        msg.saveChanges();
        Transport.send(msg);
    }
}
