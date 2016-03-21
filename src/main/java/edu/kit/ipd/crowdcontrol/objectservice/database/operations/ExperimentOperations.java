package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsCalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * responsible for the operations on the Experiment-Table
 * @author Leander K.
 * @author Niklas Keller
 */
public class ExperimentOperations extends AbstractOperations {
    private final ExperimentsPlatformOperations experimentsPlatformOperations;
    public ExperimentOperations(DSLContext create, ExperimentsPlatformOperations experimentsPlatformOperations) {
        super(create);
        this.experimentsPlatformOperations = experimentsPlatformOperations;
    }
    /**
     * inserts the Experiment into the database
     * @param experimentRecord the record to insert
     * @return the resulting id of the experiment
     */
    public int insertNewExperiment(ExperimentRecord experimentRecord) {
        return create.insertInto(EXPERIMENT)
                .set(experimentRecord)
                .returning(EXPERIMENT.ID_EXPERIMENT)
                .fetchOne()
                .value1();
    }

    /**
     * returns the experiment corresponding to the id
     * @param id the id
     * @return the optional experiment
     */
    public Optional<ExperimentRecord> getExperiment(int id) {
        return create.selectFrom(EXPERIMENT)
                .where(EXPERIMENT.ID_EXPERIMENT.eq(id))
                .fetchOptional();
    }

    /**
     * updates the experiment and returns whether it was successful
     * @param experimentRecord the record to update
     * @return true if successful, false if not
     * @throws IllegalStateException if the experiment is running
     */
    public boolean updateExperiment(ExperimentRecord experimentRecord) throws IllegalStateException {
        return doIfDraft(experimentRecord.getIdExperiment(), trans ->
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
            DSL.using(trans).deleteFrom(EXPERIMENTS_PLATFORM)
                    .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).deleteFrom(CHOSEN_ANSWER_QUALITY_PARAM)
                    .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(CHOSEN_TASK_CHOOSER_PARAM)
                    .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(CHOSEN_RATING_QUALITY_PARAM)
                    .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(RATING)
                    .where(RATING.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(RATING_RESERVATION)
                    .where(RATING_RESERVATION.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(ANSWER)
                    .where(ANSWER.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(ANSWER_RESERVATION)
                    .where(ANSWER_RESERVATION.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(CALIBRATION)
                    .where(CALIBRATION.EXPERIMENT.eq(id))
                    .execute();

            DSL.using(trans).delete(EXPERIMENTS_PLATFORM)
                    .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(id))
                    .execute();

            int deleted = DSL.using(trans)
                    .deleteFrom(EXPERIMENT)
                    .where(EXPERIMENT.ID_EXPERIMENT.eq(id))
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
        Set<ExperimentsPlatformStatusPlatformStatus> statuses = new HashSet<>(experimentsPlatformOperations
                .getExperimentsPlatformStatusPlatformStatuses(id).values());
        //TODO: what to do if one of the platforms failed?
        if (statuses.isEmpty()) {
            return Experiment.State.DRAFT;
        } else if (statuses.size() != 1) {
            //we have a inhomogeneous state, BAD!
            return Experiment.State.INVALID;
        } else if (statuses.contains(ExperimentsPlatformStatusPlatformStatus.running)) {
            return Experiment.State.PUBLISHED;
        } else if (statuses.contains(ExperimentsPlatformStatusPlatformStatus.shutdown)) {
            return Experiment.State.PUBLISHED;
        } else if (statuses.contains(ExperimentsPlatformStatusPlatformStatus.creative_stopping)) {
            return Experiment.State.CREATIVE_STOPPED;
        } else if (statuses.contains(ExperimentsPlatformStatusPlatformStatus.finished)) {
            return Experiment.State.STOPPED;
        } else {
            return Experiment.State.INVALID;
        }
    }

    /**
     * returns all calibrations of a experiment
     * @param id the primary key of the experiment
     * @return a map where the keys are the
     */
    public Map<ExperimentsPlatformRecord, List<ExperimentsCalibrationRecord>> getCalibrations(int id) {
        return create.select(EXPERIMENTS_PLATFORM.fields())
                .select(EXPERIMENTS_CALIBRATION.fields())
                .from(EXPERIMENTS_PLATFORM)
                .join(EXPERIMENTS_CALIBRATION).onKey()
                .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(id))
                .fetchGroups(EXPERIMENTS_PLATFORM, record -> record.into(EXPERIMENTS_CALIBRATION));
    }

    /**
     * returns all RatingOptions for the Experiment
     * @param experimentId the primary key of the experiment
     * @return a list of RatingOptionExperiments
     */
    public List<RatingOptionExperimentRecord> getRatingOptions(int experimentId) {
        return create.selectFrom(RATING_OPTION_EXPERIMENT)
                .where(RATING_OPTION_EXPERIMENT.EXPERIMENT.eq(experimentId))
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
        return getNextRange(create.selectFrom(EXPERIMENT), EXPERIMENT.ID_EXPERIMENT, EXPERIMENT, cursor, next, limit);
    }

    /**
     * checks whether the experiment has all the information needed for publishing
     * @param id the primary key of the experiment
     * @return true if able to publish, false if not
     */
    public boolean verifyExperimentForPublishing(int id) {
        ExperimentRecord experimentRecord = create.fetchOne(EXPERIMENT, EXPERIMENT.ID_EXPERIMENT.eq(id));
        if (experimentRecord.getTitle() == null
                || experimentRecord.getDescription() == null
                || experimentRecord.getNeededAnswers() == null
                || experimentRecord.getRatingsPerAnswer() == null
                || experimentRecord.getAnwersPerWorker() == null
                || experimentRecord.getRatingsPerWorker() == null
                || experimentRecord.getAlgorithmTaskChooser() == null
                || experimentRecord.getAlgorithmQualityAnswer() == null
                || experimentRecord.getAlgorithmQualityRating() == null
                || experimentRecord.getBasePayment() == null
                || experimentRecord.getBonusAnswer() == null
                || experimentRecord.getBonusRating() == null
                || experimentRecord.getWorkerQualityThreshold() == null
                || experimentRecord.getResultQualityThreshold() == null) {
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

    /**
     * deletes all the associated RatingOptions from the DB
     * @param experimentId the primary key of the Experiment-Table
     */
    public void deleteRatingOptions(int experimentId) {
        create.deleteFrom(RATING_OPTION_EXPERIMENT)
                .where(RATING_OPTION_EXPERIMENT.EXPERIMENT.eq(experimentId))
                .execute();
    }

    /**
     * stores the passed RatingOption, deletes the existing if they are missing in the list
     * @param ratingOptions the ratingOptions
     * @param experimentId the primary key of the Experiment-Table
     */
    public void storeRatingOptions(List<Experiment.RatingOption> ratingOptions, int experimentId) {
        assertRatingOptions(ratingOptions);

        List<RatingOptionExperimentRecord> toInsert = ratingOptions.stream()
                .map(option -> new RatingOptionExperimentRecord(null, option.getName(), option.getValue(), experimentId))
                .collect(Collectors.toList());

        create.transaction(conf -> {
            DSL.using(conf).deleteFrom(RATING_OPTION_EXPERIMENT)
                    .where(RATING_OPTION_EXPERIMENT.EXPERIMENT.eq(experimentId))
                    .execute();

            DSL.using(conf).batchInsert(toInsert).execute();
        });
    }

    /**
     * Asserts that there are at least two rating options and that their values are between 0 and 9.
     * @param ratingOptions List of rating options.
     */
    public void assertRatingOptions(List<Experiment.RatingOption> ratingOptions) {
        if (ratingOptions.size() < 2) {
            throw new IllegalArgumentException("There must be at least two ratings options.");
        }

        ratingOptions.stream().forEach(ratingOption -> {
            if (ratingOption.getValue() < 0 || ratingOption.getValue() > 9) {
                throw new IllegalArgumentException("Rating option values must be between 0 and 9.");
            }
        });
    }
}
