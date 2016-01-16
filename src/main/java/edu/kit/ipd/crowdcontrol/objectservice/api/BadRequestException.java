package edu.kit.ipd.crowdcontrol.objectservice.api;

/**
 * Thrown on invalid requests. The exception's message will be forwarded to clients.
 *
 * @author Niklas Keller
 */
public class BadRequestException extends RuntimeException {
	public BadRequestException(String message) {
		super(message);
	}
}
