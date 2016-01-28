package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Optional;

/**
 * Responsible for the operations involving the creation of tasks.
 *
 * @author LeanderK
 * @version 1.0
 */
public class TasksOperations extends AbstractOperations {
    protected TasksOperations(DSLContext create) {
        super(create);
    }

    /**
     * Inserts the TaskRecord into the database.
     *
     * @param taskRecord the task to create
     * @return the resulting TaskRecord existing in the DB
     */
    public TaskRecord createTask(TaskRecord taskRecord) {
        taskRecord.setIdTask(null);
        return doIfNotRunning(taskRecord.getExperiment(), trans ->
                DSL.using(trans)
                        .insertInto(Tables.TASK)
                        .set(taskRecord)
                        .returning()
                        .fetchOne());
    }

    /**
     * Updates a Task.
     *
     * @param taskRecord the update
     * @return whether the update was successful
     * @throws IllegalArgumentException if the record has no primary key
     */
    public boolean updateTask(TaskRecord taskRecord) throws IllegalArgumentException {
        assertHasPrimaryKey(taskRecord);
        return create.executeUpdate(taskRecord) == 1;
    }

    /**
     * Searches for a task specified by platform and experimentId.
     *
     * @param platform     the string of the platform
     * @param experimentId the primary key of the experiment
     * @return the found task or empty if not found
     */
    public Optional<TaskRecord> getTask(String platform, int experimentId) {
        return create.selectFrom(Tables.TASK)
                .where(Tables.TASK.CROWD_PLATFORM.eq(platform))
                .and(Tables.TASK.EXPERIMENT.eq(experimentId))
                .fetchOptional();
    }
}
