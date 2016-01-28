package edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions;

/**
 * Thrown on invalid requests.
 *
 * @author Niklas Keller
 */
public class NotFoundException extends BadRequestException {
    /**
     * @param message Error details. Forwarded to the client.
     * @param args    Arguments for {@link String#format(String, Object...)}.
     */
    public NotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }

    /**
     * Creates a new instance with the default text "Resource not found."
     */
    public NotFoundException() {
        this("Resource not found.");
    }
}
