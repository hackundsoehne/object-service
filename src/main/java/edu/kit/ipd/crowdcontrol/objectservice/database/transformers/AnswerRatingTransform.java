package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by marcel on 26.01.16.
 */
public class AnswerRatingTransform {

    /**
     * Convert a record into a protobuf object
     * @param answerRecord The record of a given answer to convert
     * @param ratings The list of ratings which should be appended to the Answer
     * @return The protobuf object with the given data from answerRecord and ratings
     */
    public static Answer toAnswerProto(AnswerRecord answerRecord, List<RatingRecord> ratings) {
        return Answer.newBuilder()
                .setExperimentId(answerRecord.getExperiment())
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
    public static AnswerRecord toAnswerRecord(Answer answer, int experimentId) {
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
    public static Rating toRatingProto(RatingRecord ratingRecord) {
        return Rating.newBuilder()
                .setRating(ratingRecord.getRating())
                .setTime(ratingRecord.getTimestamp().getNanos())
                .setWorker(ratingRecord.getWorkerId()).build();
    }

    /**
     * Converts a rating from a protobuf object into a record
     * The Quality of a RatingRecord cannot be set and is set to 0
     * @param rating The informations to use from protobuf
     * @param answerId the answer which was rated for
     * @param experimentId the experiment of the answer
     * @return A RatingRecord
     */
    public static RatingRecord toRatingRecord(Rating rating, int answerId, int experimentId) {
        return new RatingRecord(0,
                experimentId,
                answerId,
                new Timestamp(rating.getTime()),
                rating.getRating(),
                rating.getFeedback(),
                rating.getWorker(),
                rating.getQuality());

    }
}
