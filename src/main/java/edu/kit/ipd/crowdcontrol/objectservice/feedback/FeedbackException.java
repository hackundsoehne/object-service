package edu.kit.ipd.crowdcontrol.objectservice.feedback;


/**
 * Gets thrown, if a error during feedback sending occurs.
 */
public class FeedbackException extends Exception {

    /**
     * Creates a new feedback exception.
     * @param message message of the exception
     */
    public FeedbackException(String message) {
        super(message);
    }


    /**
     * Creates a new feedback exception.
     * @param message message of the exception
     * @param throwable cause of the exception
     */
    public FeedbackException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
