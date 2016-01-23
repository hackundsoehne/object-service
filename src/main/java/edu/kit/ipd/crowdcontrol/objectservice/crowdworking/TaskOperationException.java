package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TasksOperations;

/**
 * Created by marcel on 23.01.16.
 */
public class TaskOperationException extends Exception {

    public TaskOperationException(String msg) {
        super(msg);
    }
}
