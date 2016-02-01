package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes a parser to extract amazon giftcodes from mails.
 */
public class MailParser {

    protected static GiftCodeRecord parseAmazonGiftCode(Message msg) throws MessagingException, IOException, AmazonMailFormatChangedException{
        //Extract Message
        String message;
        try {
            Multipart parts = (Multipart) msg.getContent();
            BodyPart body = parts.getBodyPart(0);
            Multipart innerMsg = (Multipart) body.getContent();
            BodyPart textBody = innerMsg.getBodyPart(0);
            message = textBody.getContent().toString();
        } catch (ClassCastException e) {
            throw new AmazonMailFormatChangedException();
        }
        //Parse Message
        String messageStr = message.replaceAll(" ","");

        String codePatternStr = "[0-9A-Z]+(-[0-9A-Z]+)+";
        Pattern codePattern = Pattern.compile(codePatternStr);
        Matcher codeMatcher = codePattern.matcher(messageStr);

        String amountPatternStr = "[0-9]+[,|.][0-9][0-9][€]";
        Pattern amountPattern = Pattern.compile(amountPatternStr);
        Matcher amountMatcher = amountPattern.matcher(messageStr);

        if (!checkMatches(codeMatcher) || !checkMatches(amountMatcher)) {
            throw new AmazonMailFormatChangedException();
        }

        String giftCode = codeMatcher.group(0);

        String amountStr = amountMatcher.group(0);
        amountStr = amountStr.replaceAll(",", "").replaceAll("\\.","").replaceAll("€","");

        GiftCodeRecord rec = new GiftCodeRecord();
        rec.setAmount(Integer.parseInt(amountStr));
        rec.setCode(giftCode);

        return rec;
    }

    private static boolean checkMatches(Matcher matcher) {
        boolean match = true;

        if (matcher.find()) {
            int groupCount = matcher.groupCount();
            for (int j = 0; j < groupCount; j++) {
                for (int k = j; k < groupCount; k++) {
                    if (!matcher.group(j).equals(matcher.group(k))) {
                        match = false;
                    }
                }
            }
        } else {
            match = false;
        }
        return match;
    }
}
