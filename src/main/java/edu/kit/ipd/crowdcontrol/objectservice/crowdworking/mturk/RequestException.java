package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import org.jooq.lambda.UncheckedException;

/**
 * Thrown when a rest request to mturk fails
 */
public class RequestException extends UncheckedException {
    /**
     * Create a new exception
     * @param s message what exactly failed
     */
    public RequestException(String s) {
        super(new Throwable(s));
    }
}
