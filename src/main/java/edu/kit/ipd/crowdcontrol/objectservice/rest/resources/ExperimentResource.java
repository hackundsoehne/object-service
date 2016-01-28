package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.AnswerRatingTransform;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.ExperimentTransform;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.TagConstraintTransform;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Created by marcel on 25.01.16.
 */
public class ExperimentResource {
    private final ExperimentOperations experimentOperations;
    private final AnswerRatingOperations answerRatingOperations;
    private final CalibrationOperations calibrationOperations;
    private final TagConstraintsOperations tagConstraintsOperations;
    private final WorkerOperations workerOperations;

    public ExperimentResource(ExperimentOperations experimentOperations, AnswerRatingOperations answerRatingOperations, CalibrationOperations calibrationOperations, TagConstraintsOperations tagConstraintsOperations, WorkerOperations workerOperations) {
        this.experimentOperations = experimentOperations;
        this.answerRatingOperations = answerRatingOperations;
        this.calibrationOperations = calibrationOperations;
        this.tagConstraintsOperations = tagConstraintsOperations;
        this.workerOperations = workerOperations;
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
     * List all experiments
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return 20 experiments
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return experimentOperations.getExperimentsFrom(from, asc, 20)
                .map(experimentRecord -> ExperimentTransform.toProto(
                        experimentRecord,
                        experimentOperations.getExperimentState(experimentRecord.getIdExperiment()),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList())
                )
                .constructPaginated(ExperimentList.newBuilder(),ExperimentList.Builder::addAllItems);
    }

    private List<ExperimentsCalibrationRecord> convertToCalibrationRecords(Experiment experiment) {
        List<ExperimentsCalibrationRecord> calibrationRecords = new ArrayList<>();

        for (Experiment.PlatformPopulation platformPopulation : experiment.getPlatformPopulationsList()) {
            for (calibration calibration : platformPopulation.getPopulationsList()) {
                if (!calibrationOperations.getCalibration(calibration.getId()).isPresent())
                    throw new IllegalArgumentException("Calibration " + calibration.getId() + " does not exists");

                for (String answer : calibration.getAcceptedAnswersList()) {
                    ExperimentsCalibrationRecord record = new ExperimentsCalibrationRecord(
                            null,
                            experiment.getId(),
                            getOrThrow(calibrationOperations
                                    .getCalibrationAnswerOptionFromCalibrations(
                                            calibration.getId(),
                                            answer
                                    ))
                                    .getIdCalibrationAnswerOption(),
                            platformPopulation.getPlatformId(),
                            false);
                    calibrationRecords.add(record);
                }
            }
        }
        return calibrationRecords;
    }

    /**
     * Create a new experiment
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The new created experiment
     */
    public Experiment put(Request request, Response response) {
        Experiment experiment = request.attribute("input");

        ExperimentRecord record = ExperimentTransform.toRecord(experiment);
        List<TagRecord> tags = TagConstraintTransform.getTags(experiment);
        List<ConstraintRecord> constraints = TagConstraintTransform.getConstraints(experiment);

        int id = experimentOperations.insertNewExperiment(record);

        tags = tags.stream()
                .map(tagConstraintsOperations::insertTag)
                .collect(Collectors.toList());

        constraints = constraints.stream()
                .map(tagConstraintsOperations::insertConstraint)
                .collect(Collectors.toList());

        convertToCalibrationRecords(experiment).forEach(calibrationOperations::insertExperimentCalibration);

        Experiment exp = ExperimentTransform.toProto(
                getOrThrow(experimentOperations.getExperiment(id)),
                experimentOperations.getExperimentState(id),
                constraints,
                getPlatforms(id),
                tags
        );

        EventManager.EXPERIMENT_CREATE.emit(exp);

        return exp;
    }

    private Experiment fetchExperiment(int id) {
        ExperimentRecord experimentRecord = getOrThrow(experimentOperations.getExperiment(id));
        Experiment.State state = experimentOperations.getExperimentState(id);
        List<TagRecord> tagRecords = tagConstraintsOperations.getTags(id);
        List<ConstraintRecord> constraintRecords = tagConstraintsOperations.getConstraints(id);
        List<Experiment.PlatformPopulation> platforms = getPlatforms(id);

        return ExperimentTransform.toProto(experimentRecord,
                state,
                constraintRecords,
                platforms,
                tagRecords);
    }

    /**
     * Returns a experiment which was specified by :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment if it was found
     */
    public Experiment get(Request request, Response response) {
        int id = getParamInt(request, "id");

        return fetchExperiment(id);
    }

    /**
     * Will take the id of an experiment and return the platform tree with
     * all published platforms with the according calibrations
     * @param id the id of the experiment
     * @return returns a list of populations with a platform
     */
    private List<Experiment.PlatformPopulation> getPlatforms(int id) {

        Function<ExperimentsCalibrationRecord, calibration> toCalibration = record -> {
            CalibrationAnswerOptionRecord a = getOrThrow(
                    calibrationOperations.getCalibrationAnswerOption(record.getAnswer())
            );
            return getOrThrow(calibrationOperations.getCalibration(a.getCalibration()));
        };

        Function<Map.Entry<String, List<calibration>>, Experiment.PlatformPopulation> toPopulation = entry ->
                Experiment.PlatformPopulation.newBuilder()
                //.setPlatformId(entry.getKey)
                .addAllPopulations(entry.getValue())
                .build();

        return experimentOperations.getCalibrations(id).stream()
                .collect(Collectors.groupingBy(
                        ExperimentsCalibrationRecord::getReferencedPlatform,
                        Collectors.mapping(toCalibration, Collectors.toList())
                        )
                )
                .entrySet().stream()
                .map(toPopulation)
                .collect(Collectors.toList());
    }

    /**
     * Patch a experiment with the new :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment with the new attributes
     */
    public Experiment patch(Request request, Response response) {
        int id = getParamInt(request, "id");
        Experiment experiment = request.attribute("input");
        Experiment old = fetchExperiment(id);
        ExperimentRecord original = getOrThrow(experimentOperations.getExperiment(id));
        Experiment resulting;
        if (experiment.getState() != experimentOperations.getExperimentState(id)) {
            //TODO do stuff! valid state changes Draft -> Published -> Stopping
            int size = experiment.getAllFields().size();

            if (size > 1)
                throw new IllegalStateException("if you change the state nothing else can be changed");

            if (!experiment.getState().equals(Experiment.State.PUBLISHED)
                    && !experiment.getState().equals(Experiment.State.STOPPING))
                throw new IllegalArgumentException("Only Publish and Creativ_Stop is allowed as state change");

            if (experiment.getState().equals(Experiment.State.PUBLISHED)
                    && experimentOperations.verifyExperimentForPublishing(id)) {
                throw new IllegalStateException("experiment lacks information needed for publishing");
            }

            resulting = fetchExperiment(id);
            resulting = resulting.toBuilder().setState(experiment.getState()).build();
        } else {
            ExperimentRecord experimentRecord = ExperimentTransform.mergeProto(original, experiment);
            experimentRecord.setIdExperiment(id);

            //update tags if they were updated
            List<TagRecord> tags = TagConstraintTransform.getTags(experiment);
            if (!tags.isEmpty()) {
                tagConstraintsOperations.deleteAllTags(id);
                tags.forEach(tagConstraintsOperations::insertTag);
            }

            //update constraints if they were changed
            List<ConstraintRecord> constraints = TagConstraintTransform.getConstraints(experiment);
            if (!constraints.isEmpty()) {
                tagConstraintsOperations.deleteAllConstraint(id);
                constraints.forEach(tagConstraintsOperations::insertConstraint);
            }

            //update calibration records from the experiment
            List<ExperimentsCalibrationRecord> records = convertToCalibrationRecords(experiment);
            if (!records.isEmpty()) {
                calibrationOperations.deleteAllExperimentCalibration(id);
                records.forEach(calibrationOperations::insertExperimentCalibration);
            }

            //update the experiment itself
            experimentOperations.updateExperiment(experimentRecord);

            resulting = fetchExperiment(id);
        }

        EventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(old, resulting));

        return resulting;
    }

    /**
     * Delete a experiment which was specified :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return null on success
     */
    public Experiment delete(Request request, Response response) {
        int id = getParamInt(request, "id");
        Experiment experiment = fetchExperiment(id);

        if (!experimentOperations.deleteExperiment(id)) {
            throw new InternalServerErrorException("Resource not found!");
        }

        EventManager.EXPERIMENT_DELETE.emit(experiment);

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

        return answerRatingOperations.getAnswersFrom(experimentId, from, asc, 20)
                .map(answerRecord -> AnswerRatingTransform.toAnswerProto(answerRecord,
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

        AnswerRecord record = answerRatingOperations.insertNewAnswer(
                AnswerRatingTransform.toAnswerRecord(answer, experimentId)
        );

        response.status(201);
        response.header("Location","/experiment/"+experimentId+"/answers/"+answer.getId()+"");

        answer = AnswerRatingTransform.toAnswerProto(record,Collections.emptyList());

        EventManager.ANSWER_CREATE.emit(answer);

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

        if (experimentId != record.getExperiment())
            throw new IllegalArgumentException("Answer not found for the given experiment");

        return AnswerRatingTransform.toAnswerProto (record,
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

        RatingRecord r = answerRatingOperations.insertNewRating(
                    AnswerRatingTransform.toRatingRecord(rating, answerId, experimentId)
                );

        response.status(201);

        rating = AnswerRatingTransform.toRatingProto(r);

        EventManager.RATINGS_CREATE.emit(rating);

        return rating;
    }
}
