package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;

import org.junit.Before;
import org.junit.Test;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the parsing of mail with amazon giftcodes.
 *
 * @author Felix Rittler
 */
public class MailParserTest {

    private Properties props;
    private Authenticator auth;

    @Before
    public void setUp() throws Exception {
        props = new Properties();

        auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(null, null);
            }
        };
    }

    @Test
    public void testParsing() throws Exception {

        MimeMessage mail = createValidMail();

        Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(mail, "test");
        assertTrue(rec.get().getAmount() == 15);
        assertTrue(rec.get().getCode().equals("5X4F-H8359N-Q2JM"));

    }

    @Test
    public void testFalsePassword() throws Exception {
        MimeMessage mail = createValidMail();

        Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(mail, "foo");
        assertFalse(rec.isPresent());

    }

    @Test
    public void testNotFromAmazon() throws Exception {
        MimeMessage mail = createValidMail();

        mail.setFrom(new InternetAddress("foobar@baz.de"));
        Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(mail, "test");
        assertFalse(rec.isPresent());
    }

    @Test (expected = MoneyTransferException.class)
    public void testMailFormatChanged() throws Exception {


        Session session = Session.getInstance(props,auth );

        MimeMessage mail = new MimeMessage(session);
        mail.setFrom(new InternetAddress("\"Amazon.de\" <gutschein-order@gc.email.amazon.de>"));

        MimeMultipart part = new MimeMultipart();
        mail.setContent(part);

        MimeBodyPart body = new MimeBodyPart();
        part.addBodyPart(body);

        MimeMultipart innerPart = new MimeMultipart();
        body.setContent(innerPart);

        MimeBodyPart innerBody = new MimeBodyPart();


        FileReader file = new FileReader("src/test/resources/moneytransfer/parserTestMessage.txt");
        BufferedReader reader = new BufferedReader(file);

        StringBuilder content = new StringBuilder();
        String messageLine;
        while ((messageLine = reader.readLine()) != null) {
            content.append(messageLine);
            content.append(System.getProperty("line.separator"));
        }

        innerBody.setContent(content, "text/plain");

        Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(mail, "test");
    }

    protected MimeMessage createValidMail() throws Exception {
        Session session = Session.getInstance(props,auth );

        MimeMessage mail = new MimeMessage(session);
        mail.setFrom(new InternetAddress("\"Amazon.de\" <gutschein-order@gc.email.amazon.de>"));

        MimeMultipart part = new MimeMultipart();
        mail.setContent(part);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        part.addBodyPart(mimeBodyPart);

        FileReader file = new FileReader("src/test/resources/moneytransfer/parserTestMessage.txt");
        BufferedReader reader = new BufferedReader(file);

        StringBuilder content = new StringBuilder();
        String messageLine;
        while ((messageLine = reader.readLine()) != null) {
            content.append(messageLine);
            content.append(System.getProperty("line.separator"));
        }

        mimeBodyPart.setContent(content, "text/plain");

        return mail;
    }
}
