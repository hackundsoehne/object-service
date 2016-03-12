package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

/**
 * Exception if the payment request to a platform has a illegal paymentjob list
 */
public class IllegalWorkerSetException extends Exception {
    /**
     * Create the new exception
     */
    public IllegalWorkerSetException() {
        super("The list of payment Jobs need to have all workers which worked on this experiment on the given platform");
    }
}
