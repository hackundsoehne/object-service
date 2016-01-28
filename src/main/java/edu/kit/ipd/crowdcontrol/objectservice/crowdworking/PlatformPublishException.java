package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PlatformPublishException extends RuntimeException {
    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public PlatformPublishException(String message) {
        super(message);
    }
}
