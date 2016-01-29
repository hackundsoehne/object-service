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
public class AnswerRatingTransform extends AbstractTransform {

    /**
     * Convert a record into a protobuf object
     * @param answerRecord The record of a given answer to convert
     * @param ratings The list of ratings which should be appended to the Answer
     * @return The protobuf object with the given data from answerRecord and ratings
     */
    public static Answer toAnswerProto(AnswerRecord answerRecord, List<RatingRecord> ratings) {
        return builder(Answer.newBuilder())
                .set(answerRecord.getQuality(), Answer.Builder::setQuality)
                .getBuilder()
                .setExperimentId(answerRecord.getExperiment())
                .setContent(answerRecord.getAnswer())
                .setId(answerRecord.getIdAnswer())
                .setTime(answerRecord.getTimestamp().getNanos())
                .setWorker(answerRecord.getWorkerId())
                .addAllRatings(() -> ratings.stream()
                        .map(AnswerRatingTransform::toRatingProto)
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
        return merge(new AnswerRecord(), answer, (field, record) -> {
            switch (field) {
                case Answer.ID_FIELD_NUMBER: record.setIdAnswer(answer.getId());
                    break;
                case Answer.EXPERIMENT_ID_FIELD_NUMBER: record.setExperiment(experimentId);
                    break;
                case Answer.CONTENT_FIELD_NUMBER: record.setAnswer(answer.getContent());
                    break;
                case Answer.WORKER_FIELD_NUMBER: record.setWorkerId(answer.getWorker());
                    break;
                case Answer.QUALITY_FIELD_NUMBER: record.setQuality(answer.getQuality());
                    break;
            }
        });
    }

    /**
     * Converts a Database record into a protobuf Objekt
     * @param ratingRecord The Record from the database to use
     * @return the new object created from the Record
     */
    public static Rating toRatingProto(RatingRecord ratingRecord) {
        return builder(Rating.newBuilder())
                .set(ratingRecord.getRating(), Rating.Builder::setRating)
                .set(ratingRecord.getFeedback(), Rating.Builder::setFeedback)
                .getBuilder()
                .setTime(ratingRecord.getTimestamp().getNanos())
                .setWorker(ratingRecord.getWorkerId())
                .build();
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
        RatingRecord ratingRecord = new RatingRecord();
        ratingRecord.setAnswerR(answerId);
        return merge(ratingRecord, rating, (field, record) -> {
            switch (field) {
                case Rating.EXPERIMENT_ID_FIELD_NUMBER: record.setExperiment(experimentId);
                    break;
                case Rating.TIME_FIELD_NUMBER: record.setTimestamp(new Timestamp(rating.getTime()));
                    break;
                case Rating.RATING_FIELD_NUMBER: record.setRating(rating.getRating());
                    break;
                case Rating.FEEDBACK_FIELD_NUMBER: record.setFeedback(rating.getFeedback());
                    break;
                case Rating.WORKER_FIELD_NUMBER: record.setWorkerId(rating.getWorker());
                    break;
                case Rating.QUALITY_FIELD_NUMBER: record.setQuality(rating.getQuality());
                    break;
            }
        });
    }
}
