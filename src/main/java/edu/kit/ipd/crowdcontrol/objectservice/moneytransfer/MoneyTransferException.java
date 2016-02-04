package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

/**
 * Gets thrown, when an error occurs in the moneytransfer-package
 *
 * @author Felix Rittler
 */
public class MoneyTransferException extends Exception {

    public MoneyTransferException(String message) {
        super(message);
    }
}
