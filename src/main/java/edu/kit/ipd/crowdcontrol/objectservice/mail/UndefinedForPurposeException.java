package edu.kit.ipd.crowdcontrol.objectservice.mail;

/**
 * This exception gets thrown, if a properties object of the MailHandler is not sufficient for the given purpose (either send or receive mails)
 *
 * @author felix
 */
public class UndefinedForPurposeException extends Exception {

    public UndefinedForPurposeException() {
        super("The Properties given to the MailHandler are not sufficient for this function of the handler");
    }
}