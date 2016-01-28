package edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions;

/**
 * Thrown on internal errors that require code changes. Any condition that can trigger such an error
 * should be fixed.
 *
 * @author Niklas Keller
 */
public class InternalServerErrorException extends RuntimeException {
    /**
     * @param message Error details. Forwarded to the client.
     * @param args    Arguments for {@link String#format(String, Object...)}.
     */
    public InternalServerErrorException(String message, Object... args) {
        super(String.format(message, args));
    }
}
