package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

/**
 * A Exception happend before the desired action took place, the action was killed because of the given exception
 *
 * @version 1.0
 */
public class PreActionException extends Exception {

    /**
     * Create a new exception to indicate that a desired action could not take place.
     *
     * @param exception The reason why a action could not take place
     */
    public PreActionException(Exception exception) {
        super("A exception happends before the action took place", exception);
    }
}
