package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.AnswerRatingTransform;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.ExperimentTransform;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.TagConstraintTransform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getParamInt;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getQueryBool;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getQueryInt;

/**
 * Created by marcel on 25.01.16.
 */
public class ExperimentResource {
    private final ExperimentOperations experimentOperations;
    private final AnswerRatingOperations answerRatingOperations;
    private final CalibrationOperations calibrationOperations;
    private final TagConstraintsOperations tagConstraintsOperations;
    private final WorkerOperations workerOperations;
    private final PlatformOperations platformOperations;

    public ExperimentResource(ExperimentOperations experimentOperations, AnswerRatingOperations answerRatingOperations, CalibrationOperations calibrationOperations, TagConstraintsOperations tagConstraintsOperations, WorkerOperations workerOperations, PlatformOperations platformOperations) {
        this.experimentOperations = experimentOperations;
        this.answerRatingOperations = answerRatingOperations;
        this.calibrationOperations = calibrationOperations;
        this.tagConstraintsOperations = tagConstraintsOperations;
        this.workerOperations = workerOperations;
        this.platformOperations = platformOperations;
    }

    /**
     * get the value of a optional or end with a NotFoundException
     * @param c The optional to unbox
     * @param <U> The typ which should be in the optional
     * @return The type which was in the optional
     */
    private <U> U R(Optional<U> c) {
        return c.orElseThrow(() -> new NotFoundException("Resource not found!"));
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
                .map(experimentRecord -> {
                    Experiment.State state = Experiment.State.DRAFT;
                    return ExperimentTransform.toProto(experimentRecord, state,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList());
                })
                .constructPaginated(ExperimentList.newBuilder(),ExperimentList.Builder::addAllItems);
    }

    private List<ExperimentsCalibrationRecord> convertToRecords(Experiment experiment) {
        List<Experiment.PlatformPopulation> calibrations = experiment.getPlatformPopulationsList();
        List<ExperimentsCalibrationRecord> result = new ArrayList<>();

        calibrations.forEach(platformCalibrations ->
                platformCalibrations.getPopulationsList().forEach(calibration -> {

                    //check if this calibration exists
                    if (!calibrationOperations.getCalibration(calibration.getId()).isPresent())
                        throw new IllegalArgumentException("Calibration "+calibration.getId()+" does not exists");

                    //go through all possible answers and add them
                    calibration.getAcceptedAnswersList().forEach(s ->
                    {
                        ExperimentsCalibrationRecord pops = new ExperimentsCalibrationRecord(null, experiment.getId(),
                                R(calibrationOperations.getCalibrationAnswerOptionFromCalibrations(
                                        calibration.getId(), s))
                                        .getIdCalibrationAnswerOption(),
                                platformCalibrations.getPlatformId()+"", false); /*FIXME*/
                        result.add(pops);
                    });

                })
        );

        return result;
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
        List<ExperimentsCalibrationRecord> calibrations = convertToRecords(experiment);

        int id = experimentOperations.insertNewExperiment(record);

        tags = tags.stream()
                .map(tagRecord -> tagConstraintsOperations.insertTag(tagRecord))
                .collect(Collectors.toList());

        constraints = constraints.stream()
                .map(constraintRecord -> tagConstraintsOperations.insertConstraint(constraintRecord))
                .collect(Collectors.toList());

        calibrations.forEach(ExperimentsCalibrationRecord ->
            calibrationOperations.insertExperimentCalibration(ExperimentsCalibrationRecord)
        );

        return ExperimentTransform.toProto(R(experimentOperations.getExperiment(id)),
                experimentOperations.getExperimentState(id),
                constraints,
                getPlatforms(id),
                tags);
    }

    /**
     * Returns a experiment which was specified by :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment if it was found
     */
    public Experiment get(Request request, Response response) {
        int id = getParamInt(request, "id");
        ExperimentRecord experimentRecord = R(experimentOperations.getExperiment(id));
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
     * Will take the id of an experiment and return the platform tree with
     * all published platforms with the according calibrations
     * @param id
     * @return
     */
    private List<Experiment.PlatformPopulation> getPlatforms(int id) {
        List<Experiment.PlatformPopulation> platforms = new ArrayList<>();
        List<ExperimentsCalibrationRecord> records = experimentOperations.getCalibrations(id);
        Map<String, List<calibration>> convert = new HashMap<>();

        //build the list of calibrations / platforms
        records.forEach(ExperimentsCalibrationRecord -> {
            //fetch the answeroption which is used as answer to get to the actual calibration
            CalibrationAnswerOptionRecord a =
                    R(calibrationOperations.getCalibrationAnswerOption(ExperimentsCalibrationRecord.getAnswer()));
            calibration pop = R(calibrationOperations.getCalibration(a.getCalibration()));

            //add the calibration to the correct platform
            List<calibration> pops = convert.get(ExperimentsCalibrationRecord.getReferencedPlatform());
            if (pops == null) {
                pops = new ArrayList<>();
                convert.put(ExperimentsCalibrationRecord.getReferencedPlatform(), pops);
            }
            pops.add(pop);
        });

        convert.forEach((s, calibrations) -> {
            platforms.add(Experiment.PlatformPopulation.newBuilder()
                    //.setPlatformId(s)
                    .addAllPopulations(calibrations)
                    .build());
        });

        return platforms;
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
        ExperimentRecord original = R(experimentOperations.getExperiment(id));

        if (experiment.getState() != experimentOperations.getExperimentState(id)) {
            final int[] count = {0};

            experiment.getAllFields().forEach((fieldDescriptor, o) -> count[0]++);

            if (count[0] > 1)
                throw new IllegalStateException("if you change the state nothing else can be changed");
        } else {
            ExperimentRecord experimentRecord = ExperimentTransform.mergeProto(original, experiment);
            experimentRecord.setIdExperiment(id);

            //update tags if they were updated
            List<TagRecord> tags = TagConstraintTransform.getTags(experiment);
            if (!tags.isEmpty()) {
                tagConstraintsOperations.deleteAllTags(id);
                tags.forEach(tagRecord -> tagConstraintsOperations.insertTag(tagRecord));
            }

            //update constraints if they were changed
            List<ConstraintRecord> constraints = TagConstraintTransform.getConstraints(experiment);
            if (!constraints.isEmpty()) {
                tagConstraintsOperations.deleteAllConstraint(id);
                constraints.forEach(constraintRecord -> tagConstraintsOperations.insertConstraint(constraintRecord));
            }

            //update calibration records from the experiment
            List<ExperimentsCalibrationRecord> records = convertToRecords(experiment);
            if (!records.isEmpty()) {
                calibrationOperations.deleteAllExperimentCalibration(id);
                records.forEach(ExperimentsCalibrationRecord -> calibrationOperations.insertExperimentCalibration(ExperimentsCalibrationRecord));
            }

            //update the experiment itself
            experimentOperations.updateExperiment(experimentRecord);
        }
        List<TagRecord> tagRecords = tagConstraintsOperations.getTags(id);
        List<ConstraintRecord> constraintRecords = tagConstraintsOperations.getConstraints(id);
        List<Experiment.PlatformPopulation> platforms = getPlatforms(id);

        //TODO update signal

        return ExperimentTransform.toProto(
                R(experimentOperations.getExperiment(id)),
                experimentOperations.getExperimentState(id),
                constraintRecords,
                platforms,
                tagRecords);
    }

    /**
     * Delete a experiment which was specified :id
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return null on success
     */
    public Experiment delete(Request request, Response response) {
        int id = getParamInt(request, "id");

        if (!experimentOperations.deleteExperiment(id)) {
            throw new InternalServerErrorException("Resource not found!");
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

        return AnswerRatingTransform.toAnswerProto(record,Collections.emptyList());
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
        AnswerRecord record = R(answerRatingOperations.getAnswer(answerId));

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

        return AnswerRatingTransform.toRatingProto(r);
    }
}
