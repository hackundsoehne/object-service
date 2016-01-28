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

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

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
     * @throws IllegalStateException if the experiment is running
     */
    public boolean updateExperiment(ExperimentRecord experimentRecord) throws IllegalStateException {
        return doIfNotRunning(experimentRecord.getIdExperiment(), trans ->
                DSL.using(trans).executeUpdate(experimentRecord) == 1);
    }

    /**
     * deletes the experiment with the passed id if it is not running.
     * @param id the id of the experiment to delete
     * @return true if deleted, false if not
     * @throws IllegalStateException if the experiment is running
     */
    public boolean deleteExperiment(int id) throws IllegalStateException {
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
            return Experiment.State.CREATIVE_STOPPED;
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

    /**
     * checks whether the experiment has all the information needed for publishing
     * @param id the primary key of the experiment
     * @return true if able to publish, false if not
     */
    public boolean verifyExperimentForPublishing(int id) {
        ExperimentRecord experimentRecord = create.fetchOne(EXPERIMENT, EXPERIMENT.ID_EXPERIMENT.eq(id));
        if (experimentRecord.getTitel() == null
                || experimentRecord.getDescription() == null
                || experimentRecord.getNeededAnswers() == null
                || experimentRecord.getRatingsPerAnswer() == null
                || experimentRecord.getAnwersPerWorker() == null
                || experimentRecord.getRatingsPerAnswer() == null
                || experimentRecord.getAlgorithmTaskChooser() == null
                || experimentRecord.getAlgorithmQualityAnswer() == null
                || experimentRecord.getAlgorithmQualityRating() == null
                || experimentRecord.getBasePayment() == null
                || experimentRecord.getBonusAnswer() == null
                || experimentRecord.getBonusRating() == null
                || experimentRecord.getWorkerQualityThreshold() == null) {
            return false;
        }
        int ratings = create.fetchCount(
                DSL.selectFrom(RATING_OPTION_EXPERIMENT)
                        .where(RATING_OPTION_EXPERIMENT.EXPERIMENT.eq(id))
        );

        if (ratings < 2) {
            return false;
        }

        int numberParameterTaskChooser = create.fetchCount(
                DSL.selectFrom(CHOSEN_TASK_CHOOSER_PARAM)
                .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(id))
        );

        int numberNeededParameterTaskChooser = create.fetchCount(
                DSL.selectFrom(ALGORITHM_TASK_CHOOSER_PARAM)
                .where(ALGORITHM_TASK_CHOOSER_PARAM.ALGORITHM.eq(experimentRecord.getAlgorithmTaskChooser()))
        );

        if (numberParameterTaskChooser != numberNeededParameterTaskChooser) {
            return false;
        }

        int numberParameterRatingQuality = create.fetchCount(
                DSL.selectFrom(CHOSEN_RATING_QUALITY_PARAM)
                        .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(id))
        );

        int numberNeededParameterRatingQuality = create.fetchCount(
                DSL.selectFrom(ALGORITHM_RATING_QUALITY)
                        .where(ALGORITHM_RATING_QUALITY.ID_ALGORITHM_RATING_QUALITY.eq(experimentRecord.getAlgorithmQualityRating()))
        );

        if (numberParameterRatingQuality != numberNeededParameterRatingQuality) {
            return false;
        }

        int numberParameterAnswerQuality = create.fetchCount(
                DSL.selectFrom(CHOSEN_ANSWER_QUALITY_PARAM)
                        .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(id))
        );

        int numberNeededParameterAnswerQuality = create.fetchCount(
                DSL.selectFrom(ALGORITHM_ANSWER_QUALITY)
                        .where(ALGORITHM_ANSWER_QUALITY.ID_ALGORITHM_ANSWER_QUALITY.eq(experimentRecord.getAlgorithmQualityAnswer()))
        );

        if (numberParameterAnswerQuality != numberNeededParameterAnswerQuality) {
            return false;
        }

        return true;
    }
}
