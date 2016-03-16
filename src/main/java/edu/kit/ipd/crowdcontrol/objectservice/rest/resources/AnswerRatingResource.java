package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AnswerRatingTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerList;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Handles requests to answer- and rating-resources.
 *
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingResource {
    private final ExperimentOperations experimentOperations;
    private final AnswerRatingOperations answerRatingOperations;
    private final WorkerOperations workerOperations;
    private final EventManager eventManager;

    public AnswerRatingResource(ExperimentOperations experimentOperations, AnswerRatingOperations answerRatingOperations,
                                WorkerOperations workerOperations, EventManager eventManager) {
        this.experimentOperations = experimentOperations;
        this.answerRatingOperations = answerRatingOperations;
        this.workerOperations = workerOperations;
        this.eventManager = eventManager;
    }

    /**
     * get the value of a optional or end with a NotFoundException
     * @param c The optional to unbox
     * @param <U> The typ which should be in the optional
     * @return The type which was in the optional
     */
    private <U> U getOrThrow(Optional<U> c) {
        return c.orElseThrow(NotFoundException::new);
    }

    /**
     * Get answers from a experiment
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The answer if it was found
     */
    public Paginated<Integer> getAnswers(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);
        int experimentId = getParamInt(request, "id");

        return answerRatingOperations.getAnswersFrom(experimentId, from, asc, 20)
                .map(answerRecord -> AnswerRatingTransformer.toAnswerProto(answerRecord,
                        answerRatingOperations.getRatings(answerRecord.getIdAnswer())))
                .constructPaginated(AnswerList.newBuilder(), AnswerList.Builder::addAllItems);
    }

    /**
     * Creates a new Answer
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The new created answer object
     */
    public Answer putAnswer(Request request, Response response) {
        int experimentId = getParamInt(request, "id");
        Answer answer = request.attribute("input");

        //throw on ratings in the answer
        if (answer.getRatingsCount() != 0)
            throw new BadRequestException("A answer Resource can not have ratings while creating!");

        //the worker of this rating does not exist
        if (!workerOperations.getWorker(answer.getWorker()).isPresent())
            throw new BadRequestException("The given worker does not exists!");

        //check that the experiment really exists
        if (!experimentOperations.getExperiment(answer.getExperimentId()).isPresent())
            throw new BadRequestException("Experiment does not exists!");

        //check that the experiment id does not differ
        if (experimentId != answer.getExperimentId())
            throw new BadRequestException("Experiment id of call and object are differing");

        if (answer.getQuality() != 0)
            throw new BadRequestException("Quality cannot be set at creation");

        AnswerRecord record;

        try {
            AnswerRecord answerRecord = AnswerRatingTransformer.toAnswerRecord(answer, experimentId);
            answerRecord.setQuality(null);
            record = answerRatingOperations.insertNewAnswer(
                    answerRecord
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }

        response.status(201);
        response.header("Location","/experiments/" + experimentId + "/answers/" + answer.getId());

        answer = AnswerRatingTransformer.toAnswerProto(record, Collections.emptyList());

        eventManager.ANSWER_CREATE.emit(answer);

        return answer;
    }

    /**
     * Return a answer with :aid from experiment :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The answer if it was found
     */
    public Answer getAnswer(Request request, Response response) {
        int experimentId = getParamInt(request, "id");
        int answerId = getParamInt(request, "aid");
        AnswerRecord record = getOrThrow(answerRatingOperations.getAnswer(answerId));

        if (experimentId != record.getExperiment()) {
            throw new IllegalArgumentException("Answer not found for the given experiment");
        }

        return AnswerRatingTransformer.toAnswerProto (record,
                answerRatingOperations.getRatings(answerId));
    }

    /**
     * Creates a new Rating for answer :aid in experiment :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The new Created Rating
     */
    public Rating putRating(Request request, Response response) {
        int experimentId = getParamInt(request, "id");
        int answerId = getParamInt(request, "aid");
        Rating rating = request.attribute("input");

        if (rating.getQuality() != 0) {
            throw new IllegalArgumentException("Quality cannot be set when creating a Rating");
        }

        Rating result;

        try {
            result = answerRatingOperations.insertRating(rating);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }

        response.status(201);
        response.header("Location", "/experiments/" + experimentId + "/answers/" + answerId);

        eventManager.RATINGS_CREATE.emit(result);

        return rating;
    }
}
