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
    private boolean canSend;
    private boolean canFetch;
    Store store;
    Folder emailfolder;


    public MailHandler(Properties props, Authenticator auth) throws IllegalPropertiesException {
        sender = props.getProperty("sender");
        props.remove("sender");
        this.auth = auth;
        this.props = props;
        session = Session.getDefaultInstance(props, auth);
        if (props.containsKey("mail.smtp.host") && props.containsKey("mail.smtp.port")
                && props.containsKey("mail.transport.protocol") && props.containsKey("mail.smtp.auth")
                && props.containsKey("mail.smtp.starttls.enable") && props.containsKey("mail.smtp.tls")
                && props.containsKey("mail.smtp.ssl.checkserveridentity")) {
            canSend = true;
        }
        if (props.containsKey("mail.store.protocol") && props.containsKey("mail.imap.host")
                && props.containsKey("mail.imap.port") && props.containsKey("mail.imap.tls")) {
            canFetch = true;
            try{
                store = session.getStore("imap");
            } catch (NoSuchProviderException e) {
                throw new IllegalPropertiesException();
            }
            try {
                store.connect();
                emailfolder = store.getFolder("INBOX");
                emailfolder.open(Folder.READ_ONLY);
            } catch (MessagingException e) {
                throw new IllegalPropertiesException();
            }



        }
    }

    /**
     * Fetches Mails, that are younger than a certain amount of days.
     *
     * @param ageOfOldestMail The maximal age of a mail, that gets fetched in days
     * @return Returns a list of fetched mails
     */
    @Override
    public LinkedList<Message> fetchNewSince(int ageOfOldestMail) throws UndefinedForPurposeException, MessagingException {
        if (!canFetch) {
            throw new UndefinedForPurposeException();
        }
        Message[] messages = emailfolder.getMessages();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, ageOfOldestMail * -1);
        Date dateOfOldestMail = cal.getTime();
        LinkedList<Message> mails = new LinkedList<Message>();
        for (int i = 0; i < messages.length && messages[i].getReceivedDate().before(dateOfOldestMail); i++) {
            mails.add(messages[i]);
        }
        return mails;
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
        if (!canSend) {
            throw new UndefinedForPurposeException();
        }
        Message msg = new MimeMessage(session);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientMail));
        msg.setFrom(new InternetAddress(sender, sender));
        msg.setSubject(subject);
        msg.setText(message);
        msg.saveChanges();
        Transport.send(msg);
    }
}
