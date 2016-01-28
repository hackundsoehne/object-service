package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

/**
 * An exception, that gets thrown, if the parser cannot parse the mails, because the mail format changed.
 * @author Felix Rittler
 */
public class AmazonMailFormatChangedException extends Exception{

    public AmazonMailFormatChangedException() {
        super("The Parser cannot extract the giftcode from the mails, because the mail format changed. You need to adjust the parser to the new mail format.");
    }
}
