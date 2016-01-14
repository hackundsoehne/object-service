package edu.ipd.kit.crowdcontrol.objectservice.mail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by frittler on 14.01.16.
 */
public class MailHandlerTest {

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.web.de");
        props.put("mail.smtp.port", "587");
        props.put("mail.transport.protocol","smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.tls", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.host", "imap.web.de");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl", "true");
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.imap.socketFactory.fallback", "false");
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("pse2016@web.de", "pse2016ipd");
            }
        };
        MailHandler handler = new MailHandler(props, auth);

    }



    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFetchNewSince() throws Exception {
        return;

    }

    @Test
    public void testSendMail() throws Exception {
        return;

    }
}