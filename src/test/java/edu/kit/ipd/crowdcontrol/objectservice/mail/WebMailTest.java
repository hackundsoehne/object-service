package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.junit.Before;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

/**
 * @author Felix Rittler
 * @author Niklas Keller
 */
public class WebMailTest extends MailHandlerTest {
    @Before
    public void setUp() throws Exception {
        this.mail = "pse2016@web.de";
        this.folder = "Sent";

        Properties props = new Properties();
        props.put("sender", this.mail);
        props.put("mail.smtp.host", "smtp.web.de");
        props.put("mail.smtp.port", "587");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.tls", "true");
        props.put("mail.smtp.tls.enable", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.web.de");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl", "true");
        props.put("mail.imal.ssl.enable", "true");
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.imap.socketFactory.fallback", "false");
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("pse2016", "pse2016ipd");
            }
        };
        handler = new MailHandler(props, auth);
    }
}
