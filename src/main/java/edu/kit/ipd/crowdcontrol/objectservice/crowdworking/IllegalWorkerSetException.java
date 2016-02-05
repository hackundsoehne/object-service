package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

/**
 * Exception if the payment request to a platform has a illegal paymentjob list
 */
public class IllegalWorkerSetException extends Exception {
    /**
     * Create the new exception
     * @param s message to show
     */
    public IllegalWorkerSetException(String s) {
        super(s);
    }
}
