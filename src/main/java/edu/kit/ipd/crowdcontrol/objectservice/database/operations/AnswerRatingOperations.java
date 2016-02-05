package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.CalibrationAnswer;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.ANSWER;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.RATING;

/**
 * responsible for all queries related to the Answer and Rating Table
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingOperations extends AbstractOperations {
    private final CalibrationOperations calibrationOperations;
    private final WorkerCalibrationOperations workerCalibrationOperations;
    private final ExperimentOperations experimentOperations;

    public AnswerRatingOperations(DSLContext create, CalibrationOperations calibrationOperations,
                                  WorkerCalibrationOperations workerCalibrationOperations, ExperimentOperations experimentOperations) {
        super(create);
        this.calibrationOperations = calibrationOperations;
        this.workerCalibrationOperations = workerCalibrationOperations;
        this.experimentOperations = experimentOperations;
    }

    /**
     * inserts a new answer into the DB
     * @param answerRecord the record to insert
     * @return the resulting record
     */
    public AnswerRecord insertNewAnswer(AnswerRecord answerRecord) {
        answerRecord.setIdAnswer(null);
        ExperimentRecord experiment = experimentOperations.getExperiment(answerRecord.getExperiment())
                .orElseThrow(() -> new IllegalArgumentException("Illegal experiment-value in rating record."));
        if (getAnswerCount(answerRecord.getWorkerId()) >= experiment.getAnwersPerWorker()) {
            throw new IllegalStateException(
                    String.format("Worker %d already submitted the maximum of allowed ratings", answerRecord.getWorkerId())
            );
        }

        AnswerRecord result = doIfRunning(answerRecord.getExperiment(), conf ->
                DSL.using(conf)
                        .insertInto(ANSWER)
                        .set(answerRecord)
                        .returning()
                        .fetchOne()
        );
        addToExperimentCalibration(answerRecord.getWorkerId(), answerRecord.getExperiment());
        return result;

    }

    /**
     * returns the number answers a worker has submitted.
     * @param workerID the primary key of the worker
     * @return the number of answers
     */
    private int getAnswerCount(int workerID) {
        return create.fetchCount(
                DSL.selectFrom(ANSWER)
                        .where(ANSWER.WORKER_ID.eq(workerID))
        );
    }

    /**
     * inserts a new rating into the DB
     * @param ratingRecord the record to insert
     * @return the resulting record
     */
    public RatingRecord insertNewRating(RatingRecord ratingRecord) {
        ratingRecord.setIdRating(null);
        ExperimentRecord experiment = experimentOperations.getExperiment(ratingRecord.getExperiment())
                .orElseThrow(() -> new IllegalArgumentException("Illegal experiment-value in rating record."));
        if (getRatingCount(ratingRecord.getWorkerId()) >= experiment.getRatingsPerWorker()) {
            throw new IllegalStateException(
                    String.format("Worker %d already submitted the maximum of allowed ratings", ratingRecord.getWorkerId())
            );
        }
        RatingRecord result = doIfRunning(ratingRecord.getExperiment(), conf ->
                DSL.using(conf)
                        .insertInto(RATING)
                        .set(ratingRecord)
                        .returning()
                        .fetchOne()
        );
        addToExperimentCalibration(ratingRecord.getWorkerId(), ratingRecord.getExperiment());
        return result;
    }

    /**
     * returns the number of ratings a worker has submitted
     * @param workerId the workerId to check for
     * @return the number of ratings
     */
    private int getRatingCount(int workerId) {
        return create.fetchCount(
                DSL.selectFrom(RATING)
                        .where(RATING.WORKER_ID.eq(workerId))
        );
    }

    /**
     * this method adds a worker to the experiment-calibration.
     * <p>
     * Every experiment has calibration with one answer-option, which gets auto-generated when the event got published.
     * If a worker now submits a rating/answer, the worker gets linked to the calibration. This is used to exclude workers,
     * who have worked on a specific event, from working on another.
     *
     * @param workerID the worker to link to the calibration
     * @param experimentId the experiment the calibration belongs to
     */
    private void addToExperimentCalibration(int workerID, int experimentId) {
        Supplier<Optional<CalibrationAnswer>> doAdd = () -> calibrationOperations
                .getCalibrationForExperiment(experimentId)
                .map(answerOption -> workerCalibrationOperations
                        .insertAnswer(workerID, answerOption.getIdCalibrationAnswerOption())
                );

        Optional<CalibrationAnswer> result = doAdd.get();

        if (!result.isPresent()) {
            System.err.println(String.format("Database inconsistency! No calibration for experiment: %d present", experimentId));
            calibrationOperations.createExperimentsCalibration(experimentId);
            doAdd.get();
        }
    }

    /**
     * gets the answer with the passed primary key
     * @param answerID the primary key of the answer
     * @return the answerRecord or emtpy
     */
    public Optional<AnswerRecord> getAnswer(int answerID) {
        return create.fetchOptional(ANSWER, ANSWER.ID_ANSWER.eq(answerID));
    }

    /**
     * Returns a range of answers starting from {@code cursor}.
     *
     * @param cursor Pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     * @return List of answers
     */
    public Range<AnswerRecord, Integer> getAnswersFrom(int expid, int cursor, boolean next, int limit) {
        SelectConditionStep<AnswerRecord> query = create.selectFrom(ANSWER)
                .where(ANSWER.EXPERIMENT.eq(expid));
        return getNextRange(query, ANSWER.ID_ANSWER, ANSWER, cursor, next, limit);
    }

    /**
     * Get a Rating form a id
     * @param id The id to search for
     * @return a RatingRecord if it exists in the db
     */
    public Optional<RatingRecord> getRating(int id) {
        return create.fetchOptional(RATING, RATING.ID_RATING.eq(id));
    }

    /**
     * Returns the list of ratings from a answer
     * @param answerId the answer which was rated
     * @return A list of ratingRecords
     */
    public List<RatingRecord> getRatings(int answerId) {
        return create.selectFrom(RATING)
                .where(RATING.ANSWER_R.eq(answerId))
                .fetch();
    }
}
