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

    protected static GiftCodeRecord parseAmazonGiftCode(Message msg) throws MoneyTransferException {
        //Extract Message
        String message = "";
        try {
            Multipart parts = (Multipart) msg.getContent();
            BodyPart body = parts.getBodyPart(0);
            Multipart innerMsg = (Multipart) body.getContent();
            BodyPart textBody = innerMsg.getBodyPart(0);
            message = textBody.getContent().toString();
        } catch (ClassCastException | MessagingException | IOException e) {
            try {
                if (msg.getFrom()[0].toString().toLowerCase().contains("amazon.de")) {
                    throw new MoneyTransferException("The Parser cannot extract the giftcode from the mails, because the mail format changed. You need to adjust the parser to the new mail format.");
                }
            } catch (MessagingException f) {
                //Nothing to do
            }
        }
        //Parse Message
        String messageStr = message.replaceAll(" ", "");

        String codePatternStr = "[0-9A-Z]+(-[0-9A-Z]+)+";
        Pattern codePattern = Pattern.compile(codePatternStr);
        Matcher codeMatcher = codePattern.matcher(messageStr);

        String amountPatternStr = "[0-9]+[,|.][0-9][0-9][€]";
        Pattern amountPattern = Pattern.compile(amountPatternStr);
        Matcher amountMatcher = amountPattern.matcher(messageStr);

        if (!checkMatches(codeMatcher) || !checkMatches(amountMatcher)) {
            throw new MoneyTransferException("The Parser cannot extract the giftcode from the mails, because the mail format changed. You need to adjust the parser to the new mail format.");
        }

        String giftCode = codeMatcher.group(0);

        String amountStr = amountMatcher.group(0);
        amountStr = amountStr.replaceAll(",", "").replaceAll("\\.", "").replaceAll("€", "");

        GiftCodeRecord rec = new GiftCodeRecord();
        rec.setAmount(Integer.parseInt(amountStr));
        rec.setCode(giftCode);

        return rec;
    }


    private static boolean checkMatches(Matcher matcher) {
        boolean match = true;
        //checks, if the groups of the matcher are identical
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
