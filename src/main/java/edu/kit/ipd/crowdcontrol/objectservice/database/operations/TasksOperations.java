package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * responsible for the operations involving the creation of tasks.
 * @author LeanderK
 * @version 1.0
 */
public class TasksOperations extends AbstractOperations {
    protected TasksOperations(DSLContext create) {
        super(create);
    }

    /**
     * creates a Task
     * @param taskRecord the task to create
     * @return the id of the task
     */
    public int createTask(TaskRecord taskRecord) {
        return doIfNotRunning(taskRecord.getExperiment(), trans -> DSL.using(trans).executeInsert(taskRecord));
    }

    /**
     * updates a Task
     * @param taskRecord the update
     * @return whether the update was successful
     */
    public boolean updateTask(TaskRecord taskRecord) {
        return create.executeUpdate(taskRecord) == 1;
    }

    /**
     * searches for a task specified by platform and experimentid
     * @param platform The string of the platform
     * @param experimentId the experimentId
     * @return the found task
     */
    public TaskRecord searchTask(String platform, int experimentId) {

    }
}
