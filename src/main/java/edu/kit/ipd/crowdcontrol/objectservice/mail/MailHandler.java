package edu.kit.ipd.crowdcontrol.objectservice.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * A Mailhandler, that can send and fetch mails to another mail address/from a mailbox
 */
public class MailHandler implements MailFetcher, MailSender {

	private Properties props;
	private Authenticator auth;
	private Session session;
	private String sender;
	Store store;

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

		// FIXME: No heavy work in constructor!
		store.connect();
	}

	/**
	 * Fetches Mails, that are younger than a certain amount of days.
	 *
	 * @return Returns a list of fetched mails
	 */
	@Override
	public Message[] fetchUnseen() throws MessagingException {
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);

		Flags seen = new Flags(Flags.Flag.SEEN);
		FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
		return inbox.search(unseenFlagTerm);
	}

	@Override
	public Message[] fetchFolder(String name) throws MessagingException {
		Folder folder = store.getFolder(name);
		folder.open(Folder.READ_ONLY);

		return folder.getMessages();
	}

	/**
	 * Sends mails to another mail address.
	 *
	 * @param recipientMail
	 * 		The mail address, the mail gets sent
	 * @param subject
	 * 		The subject of the mail
	 * @param message
	 * 		The content of the mail
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
