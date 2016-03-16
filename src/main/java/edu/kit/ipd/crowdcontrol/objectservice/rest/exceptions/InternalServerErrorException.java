package edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions;

/**
 * Thrown on internal errors that require code changes. Any condition that can trigger such an error
 * should be fixed.
 *
 * @author Niklas Keller
 */
public class InternalServerErrorException extends RuntimeException {
    /**
     * @param message error details, forwarded to the client
     * @param args    arguments for {@link String#format(String, Object...)}
     */
    public InternalServerErrorException(String message, Object... args) {
        super(String.format(message, args));
    }

    /**
     * @param message error details, forwarded to the client
     * @param cause   exception cause, so further details can be logged
     */
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
