package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Experiment;

/**
 * Gets thrown, if a
 */
public class ExperimentNotFoundException extends Exception {

    public ExperimentNotFoundException() {
        super("The given experiment is not existent.");
    }
}
