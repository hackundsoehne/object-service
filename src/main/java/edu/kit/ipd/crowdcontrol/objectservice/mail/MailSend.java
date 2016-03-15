package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Describes a MailSender, which can send mails to other mail addresses.
 * @author Felix Rittler
 * @author Marcel Hollerbach
 */
public class MailSend implements MailSender {

    public enum Protocol {
        smtp, smtps
    }

    private final Properties properties;
    private final String from;
    private final Protocol protocol;
    private final String host;
    private final String user;
    private final String password;
    private final int port;

    /**
     * Create a new MailSender instance for the given attributes
     *
     * If you want to use ssl you can specify smtps if not just use smtp
     *
     * @param protocol the protocol to use
     * @param user user to authorize with the service
     * @param password password to use for authorize
     * @param from the senders email address
     * @param host host to use as send service
     * @param port server port to connect to
     */
    public MailSend(Protocol protocol, String user, String password, String from, String host, int port, boolean debug) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.from = from;

        properties = new Properties();

        //we can always send starttls as this will ask to server if he supports it
        properties.setProperty("mail.smtp.starttls.enable", "true");

        if (debug)
            properties.setProperty("mail.debug", true+"");
        properties.setProperty("mail." + protocol + ".ssl.checkserveridentity", "true");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(String recipientMail, String subject, String message) throws MessagingException, UnsupportedEncodingException {
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientMail));
        msg.setFrom(new InternetAddress(from, "CrowdControl"));
        msg.setContent(message, "text/html; charset=utf-8");
        msg.setSubject(subject);
        msg.setText(message);

        Transport transport = session.getTransport(protocol.toString());
        transport.connect(host, port, user, password);
        transport.sendMessage(msg, msg.getAllRecipients());
    }
}
