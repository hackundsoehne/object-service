package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.ANSWER;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.RATING;

/**
 * responsible for all queries related to the Answer and Rating Table
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingOperations extends AbstractOperations {
    public AnswerRatingOperations(DSLContext create) {
        super(create);
    }

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

    public Optional<AnswerRecord> getAnswer(int answerID) {
        return create.fetchOptional(ANSWER, ANSWER.ID_ANSWER.eq(answerID));
    }

    public Range<AnswerRecord, Integer> getAnswersFrom(int expid, int cursor, boolean next, int limit) {
        SelectConditionStep<AnswerRecord> query = create.selectFrom(ANSWER)
                .where(ANSWER.EXPERIMENT.eq(expid));
        return getNextRange(query, ANSWER.ID_ANSWER, cursor, next, limit);
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

    /**
     * Convert a record into a protobuf object
     * @param answerRecord The record of a given answer to convert
     * @param ratings The list of ratings which should be appended to the Answer
     * @return The protobuf object with the given data from answerRecord and ratings
     */
    public Answer toAnswerProto(AnswerRecord answerRecord, List<RatingRecord> ratings) {
        return Answer.newBuilder()
                .setContent(answerRecord.getAnswer())
                .setId(answerRecord.getIdAnswer())
                .setQuality(answerRecord.getQuality())
                .setTime(answerRecord.getTimestamp().getNanos())
                .setWorker(answerRecord.getWorkerId())
                .addAllRatings(() -> ratings.stream()
                        .map(this::toRatingProto)
                        .iterator()).build();
    }

    /**
     * Converts the given protobuf object into a record
     *
     * @param answer The protobuf object to convert
     * @param experimentId the experiment of the answer
     * @return The record with the same data like the answer
     */
    public AnswerRecord toAnswerRecord(Answer answer, int experimentId) {
        return new AnswerRecord(
                answer.getId(),
                experimentId,
                answer.getContent(),
                new Timestamp(answer.getTime()),
                answer.getWorker(),
                answer.getQuality(),
                false);
    }

    /**
     * Converts a Database record into a protobuf Objekt
     * @param ratingRecord The Record from the database to use
     * @return the new object created from the Record
     */
    public Rating toRatingProto(RatingRecord ratingRecord) {
        return Rating.newBuilder()
                .setRating(ratingRecord.getRating())
                .setTime(ratingRecord.getTimestamp().getNanos())
                .setWorker(ratingRecord.getWorkerId()).build();
    }

    /**
     * Converts a rating from a protobuf object into a record
     * @param rating The informations to use from protobuf
     * @param answerId the answer which was rated for
     * @param experimentId the experiment of the answer
     * @return A RatingRecord
     */
    public RatingRecord toRatingRecord(Rating rating, int answerId, int experimentId) {
        return new RatingRecord(0,
                experimentId,
                answerId,
                new Timestamp(rating.getTime()),
                rating.getRating(),
                rating.getWorker(),
                0);

    }
}
