package edu.kit.ipd.crowdcontrol.objectservice.mail;

import org.junit.Before;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

/**
 * @author Felix Rittler
 * @author Niklas Keller
 */
public class GmailMailTest extends MailHandlerTest {
	@Before
	public void setUp() throws Exception {
		this.mail = "pseipd@gmail.com";
		this.folder = "[Gmail]/Sent Mail";

		Properties props = new Properties();
		props.put("sender", this.mail);
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
}
