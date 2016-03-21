package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.ExperimentOperator;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PreActionException;
import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.PopulationsHelper;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.TagConstraintTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.ExperimentList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Handles requests to experiment resources.
 *
 * @author LeanderK
 * @author Marcel Hollerbach
 */
public class ExperimentResource {
    private static final Logger log = LogManager.getLogger("ExperimentResource");
    private final AnswerRatingOperations answerRatingOperations;
    private final ExperimentOperations experimentOperations;
    private final CalibrationOperations calibrationOperations;
    private final TagConstraintsOperations tagConstraintsOperations;
    private final AlgorithmOperations algorithmsOperations;
    private final ExperimentsPlatformOperations experimentsPlatformOperations;
    private final PlatformManager platformManager;
    private final ExperimentOperator experimentOperator;
    private final ExperimentFetcher experimentFetcher;
    private final PopulationsHelper populationsHelper;
    private final EventManager eventManager;

    public ExperimentResource(AnswerRatingOperations answerRatingOperations, ExperimentOperations experimentOperations, CalibrationOperations calibrationOperations,
                              TagConstraintsOperations tagConstraintsOperations, AlgorithmOperations algorithmsOperations,
                              ExperimentsPlatformOperations experimentsPlatformOperations, PlatformManager platformManager, ExperimentOperator experimentOperator,
                              ExperimentFetcher experimentFetcher, PopulationsHelper populationsHelper, EventManager eventManager) {
        this.experimentOperator = experimentOperator;
        this.answerRatingOperations = answerRatingOperations;
        this.experimentOperations = experimentOperations;
        this.calibrationOperations = calibrationOperations;
        this.tagConstraintsOperations = tagConstraintsOperations;
        this.algorithmsOperations = algorithmsOperations;
        this.experimentsPlatformOperations = experimentsPlatformOperations;
        this.platformManager = platformManager;
        this.experimentFetcher = experimentFetcher;
        this.eventManager = eventManager;
        this.populationsHelper = populationsHelper;
    }

    /**
     * get the value of a optional or end with a NotFoundException
     *
     * @param c   The optional to unbox
     * @param <U> The typ which should be in the optional
     * @return The type which was in the optional
     */
    private <U> U getOrThrow(Optional<U> c) {
        return c.orElseThrow(NotFoundException::new);
    }



    /**
     * List all experiments
     *
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return 20 experiments
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        // TODO: (low priority) Optimize fetchExperiment for multiple experiments
        return experimentOperations.getExperimentsFrom(from, asc, 20)
                .map(experimentRecord -> experimentFetcher.fetchExperiment(experimentRecord.getIdExperiment()))
                .constructPaginated(ExperimentList.newBuilder(), ExperimentList.Builder::addAllItems);
    }

    /**
     * Create a new experiment
     *
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The new created experiment
     */
    public Experiment put(Request request, Response response) {
        Experiment experiment = request.attribute("input");

        try {
            experimentOperations.assertRatingOptions(experiment.getRatingOptionsList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        ExperimentRecord record = ExperimentTransformer.mergeProto(new ExperimentRecord(), experiment);

        Map<String, String> placeholders = experiment.getPlaceholders();
        String rawDescription = experiment.getDescription();

        if (!Template.parse(rawDescription).keySet().equals(placeholders.keySet())) {
            throw new BadRequestException("Description and placeholder keys must match.");
        }

        record.setDescription(Template.apply(rawDescription, placeholders));

        int id = experimentOperations.insertNewExperiment(record);

        List<TagRecord> tags = TagConstraintTransformer.getTags(experiment, id);
        List<ConstraintRecord> constraints = TagConstraintTransformer.getConstraints(experiment, id);

        tags.stream()
                .filter(tagRecord -> !tagRecord.getTag().isEmpty())
                .forEach(tagConstraintsOperations::insertTag);

        constraints.stream()
                .filter(constraintRecord -> !constraintRecord.getConstraint().isEmpty())
                .forEach(tagConstraintsOperations::insertConstraint);

        experimentOperations.storeRatingOptions(experiment.getRatingOptionsList(), id);

        populationsHelper.storePopulations(id, experiment.getPopulationsList());

        experiment.getAlgorithmTaskChooser()
                .getParametersList()
                .stream()
                .filter(param -> !param.getValue().isEmpty())
                .forEach(param ->
                algorithmsOperations.storeTaskChooserParam(id, param.getId(), param.getValue())
        );

        experiment.getAlgorithmQualityAnswer()
                .getParametersList()
                .stream()
                .filter(param -> !param.getValue().isEmpty())
                .forEach(param ->
                algorithmsOperations.storeAnswerQualityParam(id, param.getId(), param.getValue())
        );
        experiment.getAlgorithmQualityRating()
                .getParametersList()
                .stream()
                .filter(param -> !param.getValue().isEmpty())
                .forEach(param ->
                algorithmsOperations.storeRatingQualityParam(id, param.getId(), param.getValue())
        );
        Experiment exp = experimentFetcher.fetchExperiment(id);

        response.status(201);
        response.header("Location", "/experiments/" + id);

        eventManager.EXPERIMENT_CREATE.emit(exp);

        return exp;
    }

    /**
     * Returns a experiment which was specified by :id
     *
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment if it was found
     */
    public Experiment get(Request request, Response response) {
        int id = getParamInt(request, "id");

        return experimentFetcher.fetchExperiment(id);
    }

    /**
     * Patch a experiment with the new :id
     *
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return The experiment with the new attributes
     */
    public Experiment patch(Request request, Response response) {
        int id = getParamInt(request, "id");
        Experiment experiment = request.attribute("input");
        Experiment old = experimentFetcher.fetchExperiment(id);
        ExperimentRecord original = getOrThrow(experimentOperations.getExperiment(id));
        Experiment resulting;

        if (experiment.getState() != Experiment.State.INVALID && experiment.getState() != experimentOperations.getExperimentState(id)) {
            int size = experiment.getAllFields().size();

            if (size > 1) {
                throw new BadRequestException("if you change the state nothing else can be changed");
            }

            try {
                resulting = updateExperimentState(id, experiment, old);
            } catch (IllegalStateException e) {
                throw new BadRequestException(e.getMessage());
            }
        } else if (old.getState() == Experiment.State.DRAFT) {
            resulting = updateExperimentInfoDraftState(id, experiment, old, original);
        } else if (old.getState() == Experiment.State.PUBLISHED) {
            resulting = updateExperimentStopgap(id, experiment, old, original);
        } else {
            throw new IllegalStateException("Patch not allowed in this state");
        }

        eventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(old, resulting));

        return resulting;
    }

    private Experiment updateExperimentStopgap(int id, Experiment experiment, Experiment old, ExperimentRecord original) {
        Set<String> existing = old.getPopulationsList().stream()
                .map(Experiment.Population::getPlatformId)
                .collect(Collectors.toSet());

        List<Experiment.Population> newPopulations = experiment.getPopulationsList().stream()
                .filter(population -> !existing.contains(population.getPlatformId()))
                .collect(Collectors.toList());

        List<String> populations = experiment.getPopulationsList().stream().map(Experiment.Population::getPlatformId).collect(Collectors.toList());

        List<String> missingPopulations = existing.stream()
                .filter(populations::contains)
                .collect(Collectors.toList());

        if (missingPopulations.size() > 0) {
            throw new BadRequestException("It's not possible to remove platforms which have already been published.");
        }

        class PlatformPopulation {
            CompletableFuture<Boolean> job;
            String population;
        }

        newPopulations.stream()
                .map(population -> {
                    try {
                        // Insert the population into the database
                        populationsHelper.insertPopulation(id, population);

                        PlatformPopulation platformPopulation = new PlatformPopulation();
                        platformPopulation.job = platformManager.publishTask(population.getPlatformId(), old);
                        platformPopulation.population = population.getPlatformId();
                        return platformPopulation;

                    } catch (PreActionException e) {
                        log.fatal("Failed to publish experiment "+ experiment +" on platform "+ population.getPlatformId(), e.getCause());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(platformPopulation -> {
                    try {
                        platformPopulation.job.join();
                    } catch (CompletionException e) {
                        log.fatal("Publishing the experiment "+experiment+ " on "+ platformPopulation.population+" failed.", e.getCause());
                    }
                });

        return experimentFetcher.fetchExperiment(id);
    }

    /**
     * updates the information about an experiment
     *
     * @param id         the id of the experiment
     * @param experiment the experiment with the new data
     * @param old        the old experiment in the protobuf-format
     * @param oldRecord  the old experiment as the database-record
     * @return the resulting experiment
     */
    private Experiment updateExperimentInfoDraftState(int id, Experiment experiment, Experiment old, ExperimentRecord oldRecord) {
        if (!old.getState().equals(Experiment.State.DRAFT)) {
            throw new IllegalStateException("When an experiment is running, only the state is allowed to be changed.");
        }

        if (!experiment.getRatingOptionsList().isEmpty()) {
            try {
                experimentOperations.assertRatingOptions(experiment.getRatingOptionsList());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

        ExperimentRecord experimentRecord = ExperimentTransformer.mergeProto(oldRecord, experiment);
        experimentRecord.setIdExperiment(id);

        Map<String, String> placeholders = experiment.getPlaceholders().size() > 0
                ? experiment.getPlaceholders()
                : old.getPlaceholders();

        String descriptionRaw = experiment.getDescription().equals("")
                ? old.getDescription()
                : experiment.getDescription();

        if (!Template.parse(descriptionRaw).keySet().equals(placeholders.keySet())) {
            throw new BadRequestException("Description and placeholder keys must match.");
        }

        experimentRecord.setDescription(Template.apply(descriptionRaw, placeholders));

        //update tags if they were updated
        List<TagRecord> tags = TagConstraintTransformer.getTags(experiment, id);
        if (!tags.isEmpty()) {
            tagConstraintsOperations.deleteAllTags(id);
            tags.stream()
                    .filter(tagRecord -> !tagRecord.getTag().isEmpty())
                    .forEach(tagConstraintsOperations::insertTag);
        }

        //update constraints if they were changed
        List<ConstraintRecord> constraints = TagConstraintTransformer.getConstraints(experiment, id);
        if (!constraints.isEmpty()) {
            tagConstraintsOperations.deleteAllConstraint(id);
            constraints.stream()
                    .filter(record -> !record.getConstraint().isEmpty())
                    .forEach(tagConstraintsOperations::insertConstraint);
        }

        populationsHelper.storePopulations(id, experiment.getPopulationsList());

        if (!Objects.equals(old.getAlgorithmTaskChooser().getName(), experimentRecord.getAlgorithmTaskChooser())) {
            algorithmsOperations.deleteChosenTaskChooserParams(id);
        }

        experiment.getAlgorithmTaskChooser().getParametersList()
                .stream()
                .filter(param -> !param.getValue().isEmpty())
                .forEach(param -> algorithmsOperations.storeTaskChooserParam(id, param.getId(), param.getValue()));

        if (!Objects.equals(old.getAlgorithmQualityAnswer().getName(), experimentRecord.getAlgorithmQualityAnswer())) {
            algorithmsOperations.deleteChosenAnswerQualityParams(id);
        }

        experiment.getAlgorithmQualityAnswer().getParametersList()
                .stream()
                .filter(param -> !param.getValue().isEmpty())
                .forEach(param -> algorithmsOperations.storeAnswerQualityParam(id, param.getId(), param.getValue()));

        if (!Objects.equals(old.getAlgorithmQualityRating().getName(), experimentRecord.getAlgorithmQualityRating())) {
            algorithmsOperations.deleteChosenRatingQualityParams(id);
        }

        experiment.getAlgorithmQualityRating().getParametersList()
                .stream()
                .filter(param -> !param.getValue().isEmpty())
                .forEach(param -> algorithmsOperations.storeRatingQualityParam(id, param.getId(), param.getValue()));

        if (!experiment.getRatingOptionsList().isEmpty()) {
            experimentOperations.storeRatingOptions(experiment.getRatingOptionsList(), id);
        }

        //update the experiment itself
        experimentOperations.updateExperiment(experimentRecord);

        return experimentFetcher.fetchExperiment(id);
    }

    /**
     * updates the state of an experiment
     *
     * @param id         the primary key of the experiment
     * @param experiment the experiment holding the new data
     * @param old        the old experiment
     * @return the resulting experiment with the new data
     */
    private Experiment updateExperimentState(int id, Experiment experiment, Experiment old) {
        //validate the only two possible changes
        if (!experiment.getState().equals(Experiment.State.PUBLISHED)
                && !experiment.getState().equals(Experiment.State.CREATIVE_STOPPED))
            throw new IllegalArgumentException("Only " + Experiment.State.PUBLISHED.name() +
                    " and " + Experiment.State.CREATIVE_STOPPED.name() +
                    " is allowed as state change");

        //validate its draft -> published
        if (experiment.getState() == Experiment.State.PUBLISHED && old.getState() != Experiment.State.DRAFT) {
            throw new BadRequestException("Publish is only allowed for experiments in draft state.");
        }

        //validate its published -> creative_stopped
        if (experiment.getState() == Experiment.State.CREATIVE_STOPPED && old.getState() != Experiment.State.PUBLISHED) {
            throw new BadRequestException("Creative stop is only allowed for published experiments.");
        }

        //check that there are enough datas for publish
        if (experiment.getState() == Experiment.State.PUBLISHED && experimentOperations.verifyExperimentForPublishing(id)) {
            throw new BadRequestException("Experiment lacks information needed for publishing.");
        }

        //check that we are not switching from shutdown into creative-stopped
        if (experiment.getState() == Experiment.State.CREATIVE_STOPPED
                && experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(id).values()
                .contains(ExperimentsPlatformStatusPlatformStatus.shutdown)) {
            throw new BadRequestException("Experiment is already shutting down.");
        }

        //create the calibration for this experiment
        if (experiment.getState().equals(Experiment.State.PUBLISHED)) {
            calibrationOperations.createExperimentsCalibration(id, experiment);
        }

        //we are here if the state has changed and changed from draft to published
        if (experiment.getState() == Experiment.State.PUBLISHED) {
            experimentOperator.startExperiment(old);
        }

        //check if we are not creative Stopped
        if (experiment.getState() == Experiment.State.CREATIVE_STOPPED) {
            //update db
            experimentsPlatformOperations.getExperimentPlatforms(id).forEach(record -> {
                experimentsPlatformOperations.setPlatformStatus(record.getIdexperimentsPlatforms(),
                        ExperimentsPlatformStatusPlatformStatus.creative_stopping);
            });
        }

        return experimentFetcher.fetchExperiment(id);
    }

    /**
     * Delete a experiment which was specified :id
     *
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return null on success
     */
    public Experiment delete(Request request, Response response) {
        int id = getParamInt(request, "id");
        Experiment experiment = experimentFetcher.fetchExperiment(id);

        try {
            if (!experimentOperations.deleteExperiment(id)) {
                throw new NotFoundException();
            }
        } catch (IllegalStateException e) {
            throw new BadRequestException("Deleting an experiment is not allowed while it is still running.");
        }

        eventManager.EXPERIMENT_DELETE.emit(experiment);

        return null;
    }
}