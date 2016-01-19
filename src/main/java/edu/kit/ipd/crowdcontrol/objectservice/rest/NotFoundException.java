package edu.kit.ipd.crowdcontrol.objectservice.rest;

/**
 * Thrown on invalid requests.
 *
 * @author Niklas Keller
 */
public class NotFoundException extends BadRequestException {
    /**
     * @param message
     *         Error details. Forwarded to the client.
     * @param args
     *         Arguments for {@link String#format(String, Object...)}.
     */
    public NotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }
}
