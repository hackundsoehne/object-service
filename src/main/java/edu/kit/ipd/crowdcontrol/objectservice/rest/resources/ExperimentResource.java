package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getParamInt;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getQueryBool;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getQueryInt;

/**
 * Created by marcel on 25.01.16.
 */
public class ExperimentResource {
    private ExperimentOperations experimentOperations;
    private AnswerRatingOperations answerRatingOperations;

    public ExperimentResource(ExperimentOperations experimentOperations, AnswerRatingOperations answerRatingOperations) {
        this.experimentOperations = experimentOperations;
        this.answerRatingOperations = answerRatingOperations;
    }

    /**
     * get the value of a optional or end with a NotFoundException
     * @param c The optional to unbox
     * @param <U> The typ which should be in the optional
     * @return The type which was in the optional
     */
    private <U> U R(Optional<U> c) {
        return c.orElseThrow(() -> new NotFoundException("Experiment not found!"));
    }

    /**
     * List all experiments
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return 20 experiments
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return experimentOperations.getExperimentsFrom(from, asc, 20)
                .constructPaginated(ExperimentList.newBuilder(),ExperimentList.Builder::addAllItems);
    }

    /**
     * Create a new experiment
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The new created experiment
     */
    public Experiment put(Request request, Response response) {
        Experiment experiment = request.attribute("input");
        int id = experimentOperations.insertNewExperiment(experimentOperations.toRecord(experiment));

        return experimentOperations.toProto(R(experimentOperations.getExperiment(id)));
    }

    /**
     * Returns a experiment which was specified by :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment if it was found
     */
    public Experiment get(Request request, Response response) {
        int id = getParamInt(request, "id");
        return experimentOperations.toProto(R(experimentOperations.getExperiment(id)));
    }

    /**
     * Patch a experiment with the new :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment with the new attributes
     */
    public Experiment patch(Request request, Response response) {
        int id = getParamInt(request, "id");

        if (!experimentOperations.updateExperiment(R(experimentOperations.getExperiment(id)))) {
            throw new InternalServerErrorException("Updating of the experiment failed!");
        }
        return experimentOperations.toProto(R(experimentOperations.getExperiment(id)));
    }

    /**
     * Delete a experiment which was specified :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return null on success
     */
    public Experiment delete(Request request, Response response) {
        int id = getParamInt(request, "id");

        /* check if experiment exists*/
        R(experimentOperations.getExperiment(id));

        if (!experimentOperations.deleteExperiment(id)) {
            throw new InternalServerErrorException("Deleting experiment failed");
        }
        return null;
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

        return R(answerRatingOperations.getAnswersFrom(experimentId, from, asc, 20))
                .map(answerRecord -> answerRatingOperations
                    .toAnswerProto(answerRecord, answerRatingOperations.getRatings(answerRecord.getIdAnswer())))
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

        if (answer.getRatingsCount() != 0) throw new BadRequestException("A answer Resource can not have ratings while creating!");

        int id = answerRatingOperations.insertNewAnswer(answerRatingOperations.toAnswerRecord(answer, experimentId));

        return answerRatingOperations.toAnswerProto(
                R(answerRatingOperations.getAnswer(experimentId, id)),
                answerRatingOperations.getRatings(id)
        );
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

        return answerRatingOperations.toAnswerProto(
                R(answerRatingOperations.getAnswer(experimentId, answerId)),
                answerRatingOperations.getRatings(answerId)
        );
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

        int id = answerRatingOperations.insertNewRating(
                    answerRatingOperations.toRatingRecord(rating, answerId, experimentId)
                );

        return answerRatingOperations.toRatingProto(R(answerRatingOperations.getRating(id)));
    }
}
