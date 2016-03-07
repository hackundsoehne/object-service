package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.junit.Before;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Felix Rittler
 * @author Niklas Keller
 */
public class WebMailTest extends MailHandlerTest {
    @Before
    public void setUp() throws Exception {
        this.mail = "pse2016@web.de";
        this.folder = "INBOX";

        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/integration-test/resources/webTest.properties"));
        properties.load(stream);
        stream.close();

        this.receiver = properties.getProperty("receiver");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("username"), properties.getProperty("password"));
            }
        };

        Properties props = new Properties();
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
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        handler = new MailHandler(props, auth, this.mail);
    }
}
