package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author felix
 */
public class MailHandlerTest2 {

    private MailHandler handler;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("sender", "pseipd@gmail.com");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        //props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.tls", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl", "true");
        props.put("mail.imap.ssl.enable", "true");
        // props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("pseipd@gmail.com", "pseboehm");
            }
        };
        handler = new MailHandler(props, auth);

    }


    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test() throws Exception {
        handler.sendMail("pseipd@gmail.com", "test", "test");
        Message[] msg = handler.fetchNewSince(8);
        assertTrue(msg[0].getSubject().equals("test"));


    }
}