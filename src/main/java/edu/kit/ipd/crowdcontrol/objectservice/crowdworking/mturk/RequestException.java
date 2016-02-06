package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

/**
 * Thrown when a rest request to mturk fails
 */
public class RequestException extends Exception {
    /**
     * Create a new exception
     * @param s message what exactly failed
     */
    public RequestException(String s) {
        super(s);
    }
}
