package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Experiment;

/**
 * Gets thrown, if a
 */
public class FeedbackException extends Exception {

    public FeedbackException(String message) {
        super(message);
    }
}
