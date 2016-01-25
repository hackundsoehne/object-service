package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import org.jooq.DSLContext;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * responsible for all queries related to the Answer and Rating Table
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingOperations extends AbstractOperations {
    public AnswerRatingOperations(DSLContext create) {
        super(create);
    }

    public int insertNewAnswer(AnswerRecord answerRecord) {
        //TODO
        //FIXME check if experiment is running
        return 0;
    }

    public int insertNewRating(RatingRecord ratingRecord) {
        //TODO
        //FIXME check if experiment is running
        return 0;
    }

    public Optional<AnswerRecord> getAnswer(int expid, int aid) {
        //TODO
        return null;
    }

    public Optional<Range<AnswerRecord, Integer>> getAnswers(int expid, int cursor, boolean next, int limit) {
        //TODO
        return null;
    }

    /**
     * Get a Rating form a id
     * @param id The id to search for
     * @return a RatingRecord if it exists in the db
     */
    public Optional<RatingRecord> getRating(int id) {
        //TODO
        return null;
    }

    /**
     * Returns the list of ratings from a answer
     * @param answerId the answer which was rated
     * @return A list of ratingRecords
     */
    public List<RatingRecord> getRatings(int answerId) {
        return null;
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
                        .map(ratingRecord -> toRatingProto(ratingRecord))
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
