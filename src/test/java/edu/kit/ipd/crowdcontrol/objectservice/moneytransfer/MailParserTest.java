package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.util.MimeMessageParser;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.jooq.util.derby.sys.Sys;
import org.junit.Before;
import org.junit.Test;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Tests the parsing of mail with amazon giftcodes.
 * @author Felix Rittler
 */
public class MailParserTest {

    protected MailHandler handler;
    protected String mail;
    private Properties props;
    private Authenticator auth;

    @Test
    public void test() throws Exception {
        Session session = Session.getInstance(props,auth);
        MimeMessage mail = new MimeMessage(session);
        MimeMultipart part = new MimeMultipart();
        mail.setContent(part);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        part.addBodyPart(mimeBodyPart);
        MimeMultipart innerPart = new MimeMultipart();
        mimeBodyPart.setContent(innerPart);
        MimeBodyPart innerBody = new MimeBodyPart();
        innerPart.addBodyPart(innerBody);
        String content = "Sie haben einen Amazon.de Geschenkgutschein erhalten! Wer Ihnen den Gutsche=\n" +
                "in geschenkt hat? Das verraten wir Ihnen weiter unten.\n" +
                "\n" +
                "BITTE LOESCHEN SIE DIESE NACHRICHT NICHT! Sie enthaelt den Code, den Sie zu=\n" +
                "m Einloesen Ihres Gutscheins benoetigen.=20\n" +
                "\n" +
                "Wir empfehlen, den Code auf Ihrer Festplatte zu speichern.\n" +
                "\n" +
                "Viel Spass beim Einkaufen wuenscht\n" +
                "\n" +
                "Amazon.de\n" +
                "http://www.amazon.de/gutscheine\n" +
                "\n" +
                "***********************************************************************\n" +
                "\n" +
                "An:=20\n" +
                "Betrag: 0,15 €" +
                "Von: Felix\n" +
                "Nachricht: test\n" +
                "\n" +
                "Gutscheincode: 5X4F-H8359N-Q2JM\n" +
                "Ablaufdatum: 27.01.2026\n" +
                "\n" +
                "\n" +
                "So einfach loesen Sie Ihren Geschenkgutschein ein:\n" +
                "\n" +
                "1. Besuchen Sie uns unter http://www.amazon.de oder http://www.amazon.at\n" +
                "\n" +
                "2. Stoebern Sie nach Ihren Wunschartikeln oder nutzen Sie unsere Suche.\n" +
                "\n" +
                "3. Legen Sie die Artikel in Ihren Einkaufswagen und klicken Sie auf \"Zur Ka=\n" +
                "sse gehen\".\n" +
                "\n" +
                "Kopieren Sie den Gutscheincode in das Feld unter \"Haben Sie einen Geschenk =\n" +
                "oder\n" +
                "Aktionsgutscheincode?\" in unserem Bestellformular.\n" +
                "\n" +
                "4. Der Gesamtbetrag wird dann automatisch um den Gutscheinwert reduziert. N=\n" +
                "ach dem Einloesen koennen Sie Ihre Bestellung durch Klicken auf den \"Bestel=\n" +
                "lung abschicken\" - Button abschliessen.\n" +
                "\n" +
                "\n" +
                "Moechten Sie den Geschenkgutschein zu einem spaeteren Zeitpunkt einloesen?\n" +
                "\n" +
                "Loesen Sie diesen einfach als Gutschein-Guthaben auf Ihrem Kundenkonto ein.=\n" +
                " Das Guthaben erscheint automatisch bei Ihrer naechsten Bestellung im Beste=\n" +
                "llformular!\n" +
                "\n" +
                "1. Gehen Sie zu \"Mein Konto\" auf unserer Website.\n" +
                "=20\n" +
                "2. Klicken Sie auf \"Geschenkgutscheinguthaben einsehen und Geschenkgutschei=\n" +
                "ne einloesen\" unter \"Zahlungseinstellungen\".=20\n" +
                "\n" +
                "3. Melden Sie sich mit Ihrer E-Mail-Adresse und Ihrem Passwort an.\n" +
                "=20\n" +
                "4. Auf der folgenden Seite geben Sie Ihren Gutscheincode ein und klicken au=\n" +
                "f \"Weiter\". Der Gutscheinbetrag wird automatisch als Gutschein-Guthaben auf=\n" +
                " Ihrem Kundenkonto gespeichert und bei der naechsten Bestellung im Bestellf=\n" +
                "ormular angezeigt.\n" +
                "\n" +
                "Weitere Informationen zur Verwendung Ihres Gutscheines finden Sie auf unser=\n" +
                "er Website unter http://www.amazon.de/einloesen\n" +
                "\n" +
                "\n" +
                "EINLOESEBEDINGUNGEN:\n" +
                "\n" +
                "Diese Bedingungen gelten fuer alle Geschenkgutscheine mit einem 14-stellige=\n" +
                "n Gutscheincode (eine Kombination aus Zahlen und Buchstaben). Fuer Aktionsg=\n" +
                "utscheine mit einem 12- oder 16-stelligen Gutscheincode gelten andere Einlo=\n" +
                "esebedingungen.\n" +
                "\n" +
                "Geschenkgutscheine koennen nur bei http://www.amazon.de oder http://www.ama=\n" +
                "zon.at bis zum Ende des dritten Jahres nach Kauf eingeloest werden. Die Ver=\n" +
                "wendung des Geschenkgutscheines fuer Artikel von Drittanbietern (z. B. Amaz=\n" +
                "on.de Marketplace), Auktionen, Zeitschriften, Resterampe oder fuer Artikel,=\n" +
                " die als Download angeboten werden, ist leider nicht moeglich. Dies gilt eb=\n" +
                "enso fuer den Kauf von weiteren Geschenkgutscheinen.\n" +
                "\n" +
                "Geschenkgutscheine werden nicht bar ausbezahlt. Ein Weiterverkauf ist ebenf=\n" +
                "alls nicht statthaft.\n" +
                "\n" +
                "Beachten Sie bitte das Ablaufdatum Ihres Gutscheines. Geschenkgutscheine un=\n" +
                "d Restguthaben von Geschenkgutscheinen sind bis zum Ablaufdatum gueltig. Re=\n" +
                "stguthaben werden bis zum Ablaufdatum des Gutscheines Ihrem Geschenkgutsche=\n" +
                "inkonto gutgeschrieben, danach koennen Sie nicht mehr verwendet werden.\n" +
                "\n" +
                "Wenn Ihre Bestellung den Betrag auf Ihrem Geschenkgutschein uebersteigt, is=\n" +
                "t die Restsumme per Kreditkarte,Bankeinzug, Rechnung oder durch die Eingabe=\n" +
                " weiterer Geschenkgutscheine auf derselben Seite zu begleichen.\n" +
                "\n" +
                "Im Falle eines Betrugs beim Gutscheinkauf oder einer Gutscheineinloesung be=\n" +
                "i www.amazon.de oder www.amazon.at ist Amazon berechtigt, die entsprechende=\n" +
                "n Kundenkonten zu schliessen und/oder eine alternative Zahlungsweise zu ver=\n" +
                "langen.\n" +
                "\n" +
                "Weitere Informationen zur Verwendung Ihres Gutscheines finden Sie auf unser=\n" +
                "er Website unter http://www.amazon.de/einloesen.\n" +
                "\n" +
                "Falls Sie noch Fragen haben - Sie erreichen uns ueber das Kontaktformular a=\n" +
                "uf unseren Hilfe-Seiten (bitte bei allen Gutscheinanfragen den GUTSCHEINCOD=\n" +
                "E in das Schreiben an uns kopieren).\n" +
                "\n" +
                "http://www.amazon.de/kontakt\n" +
                "\n" +
                "Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf =\n" +
                "diese Nachricht.\n" +
                "\n" +
                "\n" +
                "Gutschein-Code: 5X4F-H8359N-Q2JM\n" +
                "Ablaufdatum: 27.01.2026\n" +
                "Bestellnummer: 305-9574257-7301958";
        innerBody.setContent(content, "text/plain");
        MailParser.parseAmazonGiftCode(mail);

    }

    @Before
    public void setUp() throws Exception {
        this.mail = "pseipd@gmail.com";

        props = new Properties();
        props.put("sender", this.mail);
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl", "true");
        props.put("mail.imap.ssl.enable", "true");
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/test/resources/gmailLogin.properties"));
        properties.load(stream);
        stream.close();
        auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("username"), properties.getProperty("password"));
            }
        };
        handler = new MailHandler(props, auth);
    }
}
