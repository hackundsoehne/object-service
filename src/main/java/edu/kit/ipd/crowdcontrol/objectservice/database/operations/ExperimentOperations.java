package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsCalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.EXPERIMENT;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.EXPERIMENTS_CALIBRATION;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.TASK;

/**
 * responsible for the operations on the Experiment-Table
 * @author LeanderK
 * @version 1.0
 */
public class ExperimentOperations extends AbstractOperations {
    public ExperimentOperations(DSLContext create) {
        super(create);
    }
    /**
     * inserts the Experiment into the database
     * @param experimentRecord the record to insert
     * @return the resulting id of the experiment
     */
    public int insertNewExperiment(ExperimentRecord experimentRecord) {
        return create.executeInsert(experimentRecord);
    }

    /**
     * returns the experiment corresponding to the id
     * @param id the id
     * @return the optional experiment
     */
    public Optional<ExperimentRecord> getExperiment(int id) {
        return create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.ID_EXPERIMENT.eq(id))
                .fetchOptional();
    }

    /**
     * updates the experiment and returns whether it was successful
     * @param experimentRecord the record to update
     * @return true if successful, false if not
     */
    public boolean updateExperiment(ExperimentRecord experimentRecord) {
        return doIfNotRunning(experimentRecord.getIdExperiment(), trans ->
                DSL.using(trans).executeUpdate(experimentRecord) == 1);
    }

    /**
     * deletes the experiment with the passed id if it is not running.
     * @param id the id of the experiment to delete
     * @return true if deleted, false if not
     * @throws IllegalArgumentException if the experiment is running
     */
    public boolean deleteExperiment(int id) throws IllegalArgumentException {
        return doIfNotRunning(id, trans -> {
            int deleted = DSL.using(trans)
                    .deleteFrom(Tables.EXPERIMENT)
                    .where(Tables.EXPERIMENT.ID_EXPERIMENT.eq(id))
                    .execute();
            return deleted == 1;
        });
    }

    /**
     * Give the state of a experiment
     * @param id the primary key of the experiment
     * @return the state
     */
    public Experiment.State getExperimentState(int id) {
        Set<TaskStatus> taskStatuses = create.select(TASK.STATUS)
                .from(TASK)
                .where(TASK.EXPERIMENT.eq(id))
                .fetchSet(TASK.STATUS);
        if (taskStatuses.isEmpty()) {
            return Experiment.State.DRAFT;
        } else if (taskStatuses.contains(TaskStatus.running)) {
            return Experiment.State.PUBLISHED;
        } else if (taskStatuses.contains(TaskStatus.stopping)) {
            return Experiment.State.STOPPING;
        } else if (taskStatuses.contains(TaskStatus.stopped)) {
            return Experiment.State.STOPPED;
        } else {
            return Experiment.State.STOPPED; //TODO: finished
        }
    }

    /**
     * returns all calibrations of a experiment
     * @param id the primary key of the experiment
     * @return a list of ExperimentsCalibrationRecords
     */
    public List<ExperimentsCalibrationRecord> getCalibrations(int id) {
        return create.selectFrom(EXPERIMENTS_CALIBRATION)
                .where(EXPERIMENTS_CALIBRATION.REFERNCED_EXPERIMENT.eq(id))
                .fetch();
    }

    /**
     * returns the experiments starting from {@code cursor}
     * @param cursor pagination cursor
     * @param next {@code true} for next, {@code false} for previous
     * @param limit the umber of records
     * @return a list of experiments
     */
    public Range<ExperimentRecord, Integer> getExperimentsFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(EXPERIMENT), EXPERIMENT.ID_EXPERIMENT, cursor, next, limit);
    }
}
