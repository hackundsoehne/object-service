package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.*;

/**
 * Describes a parser to extract amazon giftcodes from mails.
 */
public class MailParser {

    protected static GiftCodeRecord parseMail(Message msg) throws MessagingException, IOException{
        //Extract Message
        Multipart parts = (Multipart) msg.getContent();
        BodyPart body = parts.getBodyPart(0);
        Multipart innermsg = (Multipart) body.getContent();
        BodyPart textBody = innermsg.getBodyPart(0);
        //Build Message
        StringBuilder message = new StringBuilder();
        InputStream inputStream = textBody.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String messageLine;
        while ((messageLine = bufferedReader.readLine()) != null) {
            message.append(messageLine);
            message.append(System.getProperty("line.separator"));
        }
        //Parse Message
        String giftCode = message.toString().split("Gutscheincode: ")[1];
        giftCode = giftCode.split("Ablaufdatum")[0];
        String amount = message.toString().split("Betrag: ")[1];
        amount = amount.split(" €")[0];
        amount = amount.split(",")[0].concat(amount.split(",")[1]);
        GiftCodeRecord rec = new GiftCodeRecord();
        rec.setAmount(Integer.parseInt(amount));
        rec.setCode(giftCode);
        return rec;
    }
}
