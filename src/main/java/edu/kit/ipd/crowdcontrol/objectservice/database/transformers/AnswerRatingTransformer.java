package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * handles the transformation of the Answers and Ratings from and to the protobuf-definitions
 *
 * @author Leander K.
 * @author Marcel Hollderbach
 * @author Niklas Keller
 */
public class AnswerRatingTransformer extends AbstractTransformer {
    /**
     * Convert a record into a protobuf object.
     *
     * @param answerRecord the record of a given answer to convert
     * @param ratings      the list of ratings which should be appended to the answer
     *
     * @return The protobuf object with the given data from answer record and ratings.
     */
    public static Answer toAnswerProto(AnswerRecord answerRecord, List<Rating> ratings) {
        return builder(Answer.newBuilder())
                .set(answerRecord.getQuality(), Answer.Builder::setQuality)
                .set(answerRecord.getSystemResponse(), Answer.Builder::setSystemresponse)
                .getBuilder()
                .setExperimentId(answerRecord.getExperiment())
                .setContent(answerRecord.getAnswer())
                .setId(answerRecord.getIdAnswer())
                //divide by 1000 because java uses milliseconds and proto expects seconds
                .setTime(answerRecord.getTimestamp().getTime() / 1000L)
                .setWorker(answerRecord.getWorkerId())
                .setReservation(answerRecord.getReservation())
                .addAllRatings(ratings)
                .build();
    }

    /**
     * Converts the given protobuf object into a record.
     *
     * @param answer       the protobuf object to convert
     * @param experimentId the experiment of the answer
     *
     * @return The record with the same data like the answer.
     */
    public static AnswerRecord toAnswerRecord(Answer answer, int experimentId) {
        AnswerRecord answerRecord = new AnswerRecord();
        answerRecord.setTimestamp(Timestamp.from(Instant.now()));
        answerRecord.setExperiment(experimentId);

        return merge(answerRecord, answer, (field, record) -> {
            switch (field) {
                case Answer.CONTENT_FIELD_NUMBER:
                    record.setAnswer(answer.getContent());
                    break;
                case Answer.WORKER_FIELD_NUMBER:
                    record.setWorkerId(answer.getWorker());
                    break;
                case Answer.RESERVATION_FIELD_NUMBER:
                    record.setReservation(answer.getReservation());
                    break;
                case Answer.SYSTEMRESPONSE_FIELD_NUMBER:
                    record.setSystemResponse(answer.getSystemresponse());
                    break;
            }
        });
    }

    /**
     * Converts a database record into a protobuf object.
     *
     * @param ratingRecord the record from the database to use
     *
     * @return New object created from the record.
     */
    public static Rating toRatingProto(RatingRecord ratingRecord, List<ConstraintRecord> constraints) {
        Function<ConstraintRecord, Constraint> mapper = (constraintRecord) -> Constraint.newBuilder().setId(constraintRecord.getIdConstraint()).setName(constraintRecord.getConstraint()).build();

        return builder(Rating.newBuilder())
                .set(ratingRecord.getExperiment(), Rating.Builder::setExperimentId)
                .set(ratingRecord.getRating(), Rating.Builder::setRating)
                .set(ratingRecord.getFeedback(), Rating.Builder::setFeedback)
                .getBuilder()
                //divide by 1000 because java uses milliseconds and proto expects seconds
                .setTime(ratingRecord.getTimestamp().getTime() / 1000L)
                .setWorker(ratingRecord.getWorkerId())
                .setReservation(ratingRecord.getReservation())
                .addAllViolatedConstraints(constraints.stream().map(mapper).collect(Collectors.toList()))
                .build();
    }

    /**
     * Converts a rating from a protobuf object into a record. The quality of a RatingRecord cannot
     * be set.
     *
     * @param rating       the information to use from protobuf
     * @param answerId     the answer which was rated for
     * @param experimentId the experiment of the answer
     *
     * @return A RatingRecord
     */
    public static RatingRecord toRatingRecord(Rating rating, int answerId, int experimentId) {
        RatingRecord ratingRecord = new RatingRecord();
        ratingRecord.setAnswerR(answerId);
        ratingRecord.setExperiment(experimentId);
        ratingRecord.setTimestamp(Timestamp.from(Instant.now()));
        ratingRecord.setRating(0); // in case the rating is 0, the merge doesn't call the setter

        return merge(ratingRecord, rating, (field, record) -> {
            switch (field) {
                case Rating.RATING_FIELD_NUMBER:
                    record.setRating(rating.getRating());
                    break;
                case Rating.FEEDBACK_FIELD_NUMBER:
                    record.setFeedback(rating.getFeedback());
                    break;
                case Rating.WORKER_FIELD_NUMBER:
                    record.setWorkerId(rating.getWorker());
                    break;
                case Rating.RESERVATION_FIELD_NUMBER:
                    record.setReservation(rating.getReservation());
                    break;
            }
        });
    }
}
