package edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions;

/**
 * Thrown on invalid requests.
 *
 * @author Niklas Keller
 */
public class BadRequestException extends RuntimeException {
    /**
     * @param message Error details. Forwarded to the client.
     * @param args    Arguments for {@link String#format(String, Object...)}.
     */
    public BadRequestException(String message, Object... args) {
        super(String.format(message, args));
    }
}
