package edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions;

/**
 * Thrown on conflicting requests.
 *
 * @author Niklas Keller
 */
public class ConflictException extends BadRequestException {
    /**
     * @param message Error details. Forwarded to the client.
     * @param args    Arguments for {@link String#format(String, Object...)}.
     */
    public ConflictException(String message, Object... args) {
        super(String.format(message, args));
    }
}
