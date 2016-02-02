package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.ANSWER;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.RATING;

/**
 * responsible for all queries related to the Answer and Rating Table
 *
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingOperations extends AbstractOperations {
    public AnswerRatingOperations(DSLContext create) {
        super(create);
    }


      /**
     * Gets all ratings of a specified answer
     *
     * @param answerRecord answer, whose ratings are requested
     * @return list of ratings of a specified answer
     */
    public Result<RatingRecord> getRatingsOfAnswer(AnswerRecord answerRecord){
        return null;
    }


    /**
     * Get all answers of the experiment specified by given ID
     *
     * @param expID specifying the experiment
     * @return list of all answers of a experiment
     */
    public Result<AnswerRecord> getAnswersOfExperiment(int expID){
        return null;
    }


    /**
     * Fetches all answers of the specified experiment with a quality-value equal or above
     * the given threshold
     * @param expID of the experiment
     * @param threshold specifying good answers. A good answer has at least a quality-value of given threshold
     * @return Map of workers and a set of matching answerRecords.
     */
    public Map<WorkerRecord,Set<AnswerRecord>> getGoodAnswersOfExperiment(int expID, int threshold){
        //TODO
        return null;
    }


    /**
     * Fetches all ratings of the specified experiment with a quality-value equal or above
     * the given threshold
     * @param expID of the experiment
     * @param threshold specifying good rating. A good rating has at least a quality-value of given threshold
     * @return Map of workers and a set of matching ratings.
     */
    public Map<WorkerRecord,Set<RatingRecord>> getGoodRatingsOfExperiment(int expID, int threshold){
        //TODO
        return null;
    }



    /**
     * Returns all ratings of given answer, which have a quality rating above passed threshold
     * @param answerRecord answer, whose good ratings (specified by given threshold) are returned
     * @param threshold of type int, which specifies good ratings
     * @return list of all ratings of given answer with a quality rating equal or greater than given threshold
     */
    public Result<RatingRecord> getGoodRatingsOfAnswer(AnswerRecord answerRecord, int threshold){
        return null;
    }


    /**
     * Sets quality ratings to a set of ratings
     *
     * @param map of ratings and matching qualities
     */
    public void setQualityToRatings(Map<RatingRecord,Integer> map){}

    /**
     * Sets quality rating to an answer
     * @param answer whose quality is to be set
     *    @param quality of the answer
     */
    public void setQualityToAnswer(AnswerRecord answer, int quality){}



    /**
     * inserts a new answer into the DB
     * @param answerRecord the record to insert
     * @return the resulting record
     */
    public AnswerRecord insertNewAnswer(AnswerRecord answerRecord) {
        answerRecord.setIdAnswer(null);
        return doIfRunning(answerRecord.getExperiment(), conf ->
                DSL.using(conf)
                        .insertInto(ANSWER)
                        .set(answerRecord)
                        .returning()
                        .fetchOne()
        );
    }


    /**
     * Sets the quality-assured-bit for the given answerRecord
     * This indicates, that the answers quality is unlikely to change
     * @param answerRecord whose quality-assured-bit is set
     */
    public void setAnswerQualityAssured(AnswerRecord answerRecord){
        //TODO
    }

    /**
     * inserts a new rating into the DB
     * @param ratingRecord the record to insert
     * @return the resulting record
     */
    public RatingRecord insertNewRating(RatingRecord ratingRecord) {
        ratingRecord.setIdRating(null);
        return doIfRunning(ratingRecord.getExperiment(), conf ->
                DSL.using(conf)
                        .insertInto(RATING)
                        .set(ratingRecord)
                        .returning()
                        .fetchOne()
        );
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
