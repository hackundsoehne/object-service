package edu.kit.ipd.crowdcontrol.objectservice.mail;

import edu.kit.ipd.crowdcontrol.objectservice.ConfigLoader;
import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import org.junit.Before;
import org.junit.Test;

import javax.mail.FolderClosedException;
import javax.mail.Message;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The test only works under the assumption that the receiver is configured to get the mails from notification
 *
 * @author Niklas Keller
 * @author Felix Rittler
 * @author Marcel Hollerbach
 */
public class MailHandlerTest {
    protected MailSender sender;
    protected MailFetcher fetcher;
    protected String receiver;
    protected String folder;

    @Before
    public void setUp() throws Exception {
        ConfigLoader configLoader = new ConfigLoader();
        Config config = configLoader.getConfig();

        folder = config.mail.moneyReceiver.inbox;

        MailSend.Protocol sendProtocol = MailSend.Protocol.valueOf(config.mail.notifications.protocol);
        MailReceiver.Protocol fetchProtocol = MailReceiver.Protocol.valueOf(config.mail.moneyReceiver.protocol);

        sender = new MailSend(sendProtocol,
                config.mail.notifications.auth.credentials.user,
                config.mail.notifications.auth.credentials.password,
                config.mail.notifications.from,
                config.mail.notifications.auth.server,
                config.mail.notifications.auth.port,
                true);

        receiver = config.mail.notifications.from;

        fetcher = new MailReceiver(fetchProtocol,
                config.mail.moneyReceiver.auth.credentials.user,
                config.mail.moneyReceiver.auth.credentials.password,
                config.mail.moneyReceiver.auth.server,
                config.mail.moneyReceiver.auth.port,
                config.mail.moneyReceiver.inbox,
                true);
    }

    @Test
    public void testSendAndFetch() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String subject = "[test] " + uuid;


        sender.sendMail(receiver, subject, uuid);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        Message[] messages = fetcher.fetchFolder(folder);

        boolean found = false;

        for (Message message : messages) {
            if (message.getSubject().equals(subject)) {
                found = true;
                fetcher.deleteMails(message);
                break;
            }
        }
        assertTrue(found);

        messages = fetcher.fetchFolder(folder);
        found = false;
        for (Message message : messages) {
            if (message.getSubject().equals(subject)) {
                found = true;
                break;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testFetchUnseenMarkAsUnseen() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String subject = "[test] " + uuid;

        sender.sendMail(receiver, subject, uuid);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        Message[] messages = fetcher.fetchUnseen(folder);

        boolean found = false;

        Message testMessage = null;
        for (Message message : messages) {
            if (message.getSubject().equals(subject)) {
                found = true;
                testMessage = message;
                fetcher.markAsSeen(message);
                break;
            }
        }
        assertTrue(found);

        messages = fetcher.fetchUnseen(folder);

        found = false;
        for (Message message : messages) {
            if (message.getSubject().equals(subject)) {
                found = true;
                break;
            }
        }
        assertFalse(found);

        fetcher.markAsUnseen(testMessage);
        messages = fetcher.fetchUnseen(folder);
        found = false;
        for (Message message : messages) {
            if (message.getSubject().equals(subject)) {
                found = true;
                fetcher.deleteMails(message);
                break;
            }
        }
        assertTrue(found);
    }

    @Test(expected = FolderClosedException.class)
    public void testOpenAndClose() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String subject = "[test] " + uuid;

        sender.sendMail(receiver, subject, uuid);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        Message[] messages = fetcher.fetchFolder(folder);
        fetcher.close(messages[0].getFolder());
        messages[0].getSubject();
    }
}