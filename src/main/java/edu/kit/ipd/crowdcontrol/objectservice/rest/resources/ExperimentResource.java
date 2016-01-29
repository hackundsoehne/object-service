package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransform;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.TagConstraintTransform;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.ExperimentList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final AlgorithmOperations algorithmsOperations;

    public ExperimentResource(ExperimentOperations experimentOperations, AnswerRatingOperations answerRatingOperations, CalibrationOperations calibrationOperations, TagConstraintsOperations tagConstraintsOperations, WorkerOperations workerOperations, AlgorithmOperations algorithmsOperations) {
        this.experimentOperations = experimentOperations;
        this.answerRatingOperations = answerRatingOperations;
        this.calibrationOperations = calibrationOperations;
        this.tagConstraintsOperations = tagConstraintsOperations;
        this.workerOperations = workerOperations;
        this.algorithmsOperations = algorithmsOperations;
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
                        experimentOperations.getExperimentState(experimentRecord.getIdExperiment()))
                )
                .constructPaginated(ExperimentList.newBuilder(),ExperimentList.Builder::addAllItems);
    }

    private List<ExperimentsCalibrationRecord> convertToCalibrationRecords(Experiment experiment) {
        List<ExperimentsCalibrationRecord> calibrationRecords = new ArrayList<>();

        for (Experiment.Population platformPopulation : experiment.getPopulationsList()) {
            for (Calibration calibration : platformPopulation.getCalibrationList()) {
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

        ExperimentRecord record = ExperimentTransform.mergeProto(new ExperimentRecord(), experiment);
        List<TagRecord> tags = TagConstraintTransform.getTags(experiment);
        List<ConstraintRecord> constraints = TagConstraintTransform.getConstraints(experiment);

        int id = experimentOperations.insertNewExperiment(record);

        tags.forEach(tagConstraintsOperations::insertTag);
        constraints.forEach(tagConstraintsOperations::insertConstraint);
        convertToCalibrationRecords(experiment).forEach(calibrationOperations::insertExperimentCalibration);

        Experiment exp = fetchExperiment(id);

        EventManager.EXPERIMENT_CREATE.emit(exp);

        return exp;
    }

    private Experiment fetchExperiment(int id) {
        ExperimentRecord experimentRecord = getOrThrow(experimentOperations.getExperiment(id));
        Experiment.State state = experimentOperations.getExperimentState(id);
        List<RatingOptionExperimentRecord> ratingOptions = experimentOperations.getRatingOptions(id);
        List<TagRecord> tagRecords = tagConstraintsOperations.getTags(id);
        List<ConstraintRecord> constraintRecords = tagConstraintsOperations.getConstraints(id);
        List<Experiment.Population> platforms = getPopulation(id);
        AlgorithmTaskChooserRecord taskChooserRecord = getOrThrow(
                algorithmsOperations.getTaskChooser(experimentRecord.getAlgorithmTaskChooser())
        );
        Map<AlgorithmTaskChooserParamRecord, String> taskChooserParams = algorithmsOperations.getTaskChooserParams(
                experimentRecord.getAlgorithmTaskChooser(), experimentRecord.getIdExperiment());
        AlgorithmAnswerQualityRecord answerQualityRecord = getOrThrow(
                algorithmsOperations.getAnswerQualityRecord(experimentRecord.getAlgorithmQualityAnswer())
        );
        Map<AlgorithmAnswerQualityParamRecord, String> answerQualityParams = algorithmsOperations.getAnswerQualityParams(
                experimentRecord.getAlgorithmQualityAnswer(), experimentRecord.getIdExperiment());
        AlgorithmRatingQualityRecord ratingQualityRecord = getOrThrow(
                algorithmsOperations.getRatingQualityRecord(experimentRecord.getAlgorithmQualityRating())
        );
        Map<AlgorithmRatingQualityParamRecord, String> ratingQualityParams =
                algorithmsOperations.getRatingQualityParams(experimentRecord.getAlgorithmQualityRating(), experimentRecord.getIdExperiment());

        return ExperimentTransform.toProto(experimentRecord,
                state,
                constraintRecords,
                platforms,
                tagRecords,
                ratingOptions,
                taskChooserRecord,
                taskChooserParams,
                answerQualityRecord,
                answerQualityParams,
                ratingQualityRecord,
                ratingQualityParams);
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
    private List<Experiment.Population> getPopulation(int id) {

        Function<ExperimentsCalibrationRecord, Calibration> toCalibration = record -> {
            CalibrationAnswerOptionRecord a = getOrThrow(
                    calibrationOperations.getCalibrationAnswerOption(record.getAnswer())
            );
            return getOrThrow(calibrationOperations.getCalibration(a.getCalibration()));
        };

        Function<Map.Entry<String, List<Calibration>>, Experiment.Population> toPopulation = entry ->
                Experiment.Population.newBuilder()
                .setPlatformId(entry.getKey())
                .addAllCalibration(entry.getValue())
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
            int size = experiment.getAllFields().size();

            if (size > 1)
                throw new IllegalStateException("if you change the state nothing else can be changed");

            //validate the only two possible changes
            if (!experiment.getState().equals(Experiment.State.PUBLISHED)
                    && !experiment.getState().equals(Experiment.State.CREATIVE_STOPPED))
                throw new IllegalArgumentException("Only "+ Experiment.State.PUBLISHED.name()+
                        " and " +Experiment.State.CREATIVE_STOPPED.name()+
                        " is allowed as state change");

            //validate its draft -> published
            if (!(experiment.getState().equals(Experiment.State.PUBLISHED) && old.getState().equals(Experiment.State.DRAFT))) {
                throw new IllegalArgumentException("Publish is only allowed for DRAFT experiments");
            }

            //validate its published -> creative_stopped
            if (!(experiment.getState().equals(Experiment.State.CREATIVE_STOPPED) && old.getState().equals(Experiment.State.PUBLISHED))) {
                throw new IllegalArgumentException("Creative stop is only allowed for published experiments");
            }

            //check that there are enough datas for publish
            if (experiment.getState().equals(Experiment.State.PUBLISHED)
                    && experimentOperations.verifyExperimentForPublishing(id)) {
                throw new IllegalStateException("experiment lacks information needed for publishing");
            }

            //TODO check if there is a chance to creative stopp is not possible? like no creative answers?

            resulting = fetchExperiment(id);
            resulting = resulting.toBuilder().setState(experiment.getState()).build();
        } else {
            if (!old.getState().equals(Experiment.State.DRAFT)) {
                throw new IllegalStateException("When an experiment is running, only the state is allowed to be changed.");
            }

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

            if (!old.getAlgorithmTaskChooser().getName().equals(experimentRecord.getAlgorithmTaskChooser())) {
                algorithmsOperations.deleteChosenTaskChooserParams(id);
            }

            experiment.getAlgorithmTaskChooser().getParametersList().forEach(param -> {
                algorithmsOperations.storeTaskChooserParam(id, param.getId(), param.getValue());
            });

            if (!old.getAlgorithmQualityAnswer().getName().equals(experimentRecord.getAlgorithmQualityAnswer())) {
                algorithmsOperations.deleteChosenAnswerQualityParams(id);
            }

            experiment.getAlgorithmQualityAnswer().getParametersList().forEach(param -> {
                algorithmsOperations.storeAnswerQualityParam(id, param.getId(), param.getValue());
            });

            if (!old.getAlgorithmQualityRating().getName().equals(experimentRecord.getAlgorithmQualityRating())) {
                algorithmsOperations.deleteChosenRatingQualityParams(id);
            }

            experiment.getAlgorithmQualityRating().getParametersList().forEach(param -> {
                algorithmsOperations.storeRatingQualityParam(id, param.getId(), param.getValue());
            });

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

        try {
            if (!experimentOperations.deleteExperiment(id)) {
                throw new NotFoundException();
            }
        } catch (IllegalStateException e) {
            throw new BadRequestException("Deleting an experiment is not allowed while it is still running.");
        }

        EventManager.EXPERIMENT_DELETE.emit(experiment);

        return null;
    }
}
