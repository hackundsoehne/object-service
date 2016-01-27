package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Tests the parsing of mail with amazon giftcodes.
 * @author Felix Rittler
 */
public class MailParserTest {

    protected MailHandler handler;
    protected String mail;

    @Test
    public void test() throws Exception {
        boolean msgCorrect;
        Message[] msgs = handler.fetchFolder("inbox");
        for (int i = 0; i < msgs.length; i++) {
            System.out.println(msgs[i].getContentType());
            if(msgs[i].getContentType()==null){}
        }
    }

    @Before
    public void setUp() throws Exception {
        this.mail = "pseipd@gmail.com";

        Properties props = new Properties();
        props.put("sender", this.mail);
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl", "true");
        props.put("mail.imap.ssl.enable", "true");
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/integration-test/resources/gmailLogin.properties"));
        properties.load(stream);
        stream.close();
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("username"), properties.getProperty("password"));
            }
        };
        handler = new MailHandler(props, auth);
    }
}
