package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

/**
 * Gets thrown, when an error occurs in the moneytransfer-package
 *
 * @author Felix Rittler
 */
public class MoneyTransferException extends Exception {

    /**
     * Creates a new MoneyTransferException
     * @param message the message of the exception
     */
    public MoneyTransferException(String message) {
        super(message);
    }


    /**
     * Creates a new MoneyTransferException
     * @param message the message of the exception
     */
    public MoneyTransferException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
