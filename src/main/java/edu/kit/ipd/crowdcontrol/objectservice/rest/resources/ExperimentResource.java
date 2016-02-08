package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.TaskOperationException;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AlgorithmsTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.TagConstraintTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.ExperimentList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.*;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Handles requests to experiment resources.
 *
 * @author LeanderK
 * @author Marcel Hollerbach
 */
public class ExperimentResource {
    private final ExperimentOperations experimentOperations;
    private final CalibrationOperations calibrationOperations;
    private final TagConstraintsOperations tagConstraintsOperations;
    private final AlgorithmOperations algorithmsOperations;
    private final TasksOperations tasksOperations;
    private final PlatformManager platformManager;
    private static final Logger log = LogManager.getLogger("ExperimentResource");

    public ExperimentResource(ExperimentOperations experimentOperations, CalibrationOperations calibrationOperations,
                              TagConstraintsOperations tagConstraintsOperations, AlgorithmOperations algorithmsOperations,
                              TasksOperations tasksOperations, PlatformManager platformManager) {
        this.experimentOperations = experimentOperations;
        this.calibrationOperations = calibrationOperations;
        this.tagConstraintsOperations = tagConstraintsOperations;
        this.algorithmsOperations = algorithmsOperations;
        this.tasksOperations = tasksOperations;
        this.platformManager = platformManager;

        rx.Observable<Event<ChangeEvent<Experiment>>> observable = EventManager.EXPERIMENT_CHANGE.getObservable();
        observable.subscribe(experimentEvent -> {
            if (experimentEvent.getData().getOld().getState() != Experiment.State.PUBLISHED
                    && experimentEvent.getData().getNeww().getState() == Experiment.State.PUBLISHED){
                startExperiment(experimentEvent.getData().getNeww());
            }
        });

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

    private void startExperiment(Experiment experiment) {
        List<Experiment.Population> successfulOps = new LinkedList<>();
        for (Experiment.Population population :
                experiment.getPopulationsList()) {
            try {
                platformManager.publishTask(population.getPlatformId(), experiment).join();
                successfulOps.add(population);

            } catch (TaskOperationException e) {
                log.fatal(String.format("Error! Could not create experiment on platform %s!", population.getPlatformId()), e);
            } catch (Exception e) {

                log.fatal("Error! Could not create experiment! " + e.getMessage());
            }
        }

        if (successfulOps.size() != experiment.getPopulationsList().size()) {
            for (Experiment.Population population :
                    successfulOps) {
                try {
                    platformManager.unpublishTask(population.getPlatformId(), experiment).join();
                } catch (TaskOperationException e) {
                    log.fatal("Fatal ERROR! Cannot unpublish!" + e.getMessage());
                } catch (Exception e) {
                    log.fatal("Fatal Error! Was not able to unpublish experiment" + e.getMessage());
                }
            }
        }

    }


    public void endExperiment(Experiment experiment) {
        for (Experiment.Population population :
                experiment.getPopulationsList()) {
            try {
                platformManager.unpublishTask(population.getPlatformId(), experiment).join();
            } catch (TaskOperationException e) {
                log.fatal("Fatal ERROR! Cannot unpublish!" + e.getMessage());
            } catch (Exception e) {
                log.fatal("Fatal Error! Was not able to unpublish experiment" + e.getMessage());
            }
        }

        try { //TODO Introduce a more elaborate solution - W. Churchill
            TimeUnit.HOURS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experiment.toBuilder().setState(Experiment.State.STOPPED).build()));
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
                .map(experimentRecord -> fetchExperiment(experimentRecord.getIdExperiment()))
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

        ExperimentRecord record = ExperimentTransformer.mergeProto(new ExperimentRecord(), experiment);

        Map<String, String> placeholders = experiment.getPlaceholders();
        String description = experiment.getDescription();

        if (!Template.parse(description).keySet().equals(placeholders.keySet())) {
            throw new BadRequestException("Description and placeholder keys must match.");
        }

        int id = experimentOperations.insertNewExperiment(record);

        List<TagRecord> tags = TagConstraintTransformer.getTags(experiment, id);
        List<ConstraintRecord> constraints = TagConstraintTransformer.getConstraints(experiment, id);

        tags.stream()
                .filter(tagRecord -> !tagRecord.getTag().isEmpty())
                .forEach(tagConstraintsOperations::insertTag);

        constraints.stream()
                .filter(constraintRecord -> !constraintRecord.getConstraint().isEmpty())
                .forEach(tagConstraintsOperations::insertConstraint);

        experiment.getPopulationsList().forEach(population -> {
            storePopulation(id, population);
        });

        experiment.getAlgorithmTaskChooser()
                .getParametersList().forEach(param ->
                algorithmsOperations.storeTaskChooserParam(id, param.getId(), param.getValue())
        );

        experiment.getAlgorithmQualityAnswer()
                .getParametersList().forEach(param ->
                algorithmsOperations.storeAnswerQualityParam(id, param.getId(), param.getValue())
        );
        experiment.getAlgorithmQualityRating()
                .getParametersList().forEach(param ->
                algorithmsOperations.storeRatingQualityParam(id, param.getId(), param.getValue())
        );
        Experiment exp = fetchExperiment(id);

        response.status(201);
        response.header("Location", "/experiments/" + id);

        EventManager.EXPERIMENT_CREATE.emit(exp);

        return exp;
    }

    private void storePopulation(int experimentId, Experiment.Population population) {
        List<Integer> answerIDs = population.getCalibrationsList().stream()
                .flatMap(calibration -> calibration.getAcceptedAnswersList().stream())
                .map(Calibration.Answer::getId)
                .collect(Collectors.toList());
        calibrationOperations.storeExperimentCalibrations(population.getPlatformId(), answerIDs, experimentId);
    }

    private Experiment fetchExperiment(int id) {
        ExperimentRecord experimentRecord = getOrThrow(experimentOperations.getExperiment(id));
        Experiment.State state = experimentOperations.getExperimentState(id);
        List<RatingOptionExperimentRecord> ratingOptions = experimentOperations.getRatingOptions(id);
        List<TagRecord> tagRecords = tagConstraintsOperations.getTags(id);
        List<ConstraintRecord> constraintRecords = tagConstraintsOperations.getConstraints(id);
        List<Experiment.Population> populations = getPopulations(id);
        AlgorithmOption taskChooser = algorithmsOperations.getTaskChooser(experimentRecord.getAlgorithmTaskChooser())
                .map(record -> AlgorithmsTransformer.toTaskChooserProto(record, algorithmsOperations.getTaskChooserParams(
                        experimentRecord.getAlgorithmTaskChooser(), experimentRecord.getIdExperiment())))
                .orElse(null);
        AlgorithmOption answerQuality = algorithmsOperations.getAnswerQualityRecord(experimentRecord.getAlgorithmQualityAnswer())
                .map(record -> AlgorithmsTransformer.toAnswerQualityProto(record, algorithmsOperations.getAnswerQualityParams(
                        experimentRecord.getAlgorithmQualityAnswer(), experimentRecord.getIdExperiment())))
                .orElse(null);

        AlgorithmOption ratingQuality = algorithmsOperations.getRatingQualityRecord(experimentRecord.getAlgorithmQualityRating())
                .map(record -> AlgorithmsTransformer.toRatingQualityProto(record, algorithmsOperations.getRatingQualityParams(
                        experimentRecord.getAlgorithmQualityRating(), experimentRecord.getIdExperiment())))
                .orElse(null);

        return ExperimentTransformer.toProto(experimentRecord,
                state,
                constraintRecords,
                populations,
                tagRecords,
                ratingOptions,
                taskChooser,
                answerQuality,
                ratingQuality);
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

        return fetchExperiment(id);
    }

    /**
     * Will take the id of an experiment and return the platform tree with
     * all published platforms with the according calibrations
     *
     * @param id the id of the experiment
     * @return returns a list of populations with a platform
     */
    private List<Experiment.Population> getPopulations(int id) {
        Function<ExperimentsCalibrationRecord, Calibration.Builder> toCalibration = record -> {
            CalibrationAnswerOptionRecord acceptedAnswer = calibrationOperations.getCalibrationAnswerOption(record.getAnswer())
                    .orElseThrow(() -> new InternalServerErrorException(String.format("CalibrationAnswerOption: %s not found", record.getAnswer())));

            Calibration.Answer answer = Calibration.Answer.newBuilder()
                    .setAnswer(acceptedAnswer.getAnswer())
                    .setId(acceptedAnswer.getIdCalibrationAnswerOption())
                    .build();

            return calibrationOperations.getCalibration(acceptedAnswer.getCalibration())
                    .map(calibration -> calibration.toBuilder().addAcceptedAnswers(answer))
                    .orElseThrow(() -> new InternalServerErrorException(String.format("Calibration: %d not found", acceptedAnswer.getCalibration())));
        };

        Function<Map.Entry<String, List<Calibration.Builder>>, Experiment.Population> toPopulation = entry -> {
            //normalize, multiple chosen answers from the same Calibration were multiple objects
            List<Calibration> calibrations = entry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            Calibration.Builder::getId,
                            Collectors.toList())
                    ).values().stream()
                    .map(list -> list.stream().reduce((o1, o2) -> o1.addAllAcceptedAnswers(o2.getAcceptedAnswersList())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(Calibration.Builder::build)
                    .collect(Collectors.toList());

            return Experiment.Population.newBuilder()
                    .setPlatformId(entry.getKey())
                    .addAllCalibrations(calibrations)
                    .build();
        };


        Map<String, List<Calibration.Builder>> populations = experimentOperations.getCalibrations(id).stream()
                .collect(Collectors.groupingBy(
                        ExperimentsCalibrationRecord::getReferencedPlatform,
                        Collectors.mapping(toCalibration, Collectors.toList())
                        )
                );

        experimentOperations.getActivePlatforms(id)
                .forEach(platform -> {
                    if (!populations.containsKey(platform)) {
                        populations.put(platform, Collections.emptyList());
                    }
                });

        return populations
                .entrySet().stream()
                .map(toPopulation)
                .collect(Collectors.toList());
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
        Experiment old = fetchExperiment(id);
        ExperimentRecord original = getOrThrow(experimentOperations.getExperiment(id));
        Experiment resulting;

        if (experiment.getState() != Experiment.State.INVALID && experiment.getState() != experimentOperations.getExperimentState(id)) {
            int size = experiment.getAllFields().size();

            if (size > 1) {
                throw new IllegalStateException("if you change the state nothing else can be changed");
            }

            resulting = updateExperimentState(id, experiment, old);
        } else if (experiment.getState() == Experiment.State.DRAFT) {
            resulting = updateExperimentInfoDraftState(id, experiment, old, original);
        } else if (experiment.getState() == Experiment.State.PUBLISHED ||
                experiment.getState() == Experiment.State.CREATIVE_STOPPED) {
            resulting = updateExperimentGapStop(id, experiment, old, original);
        } else {
            throw new IllegalStateException("Patch not allowed in this state");
        }

        EventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(old, resulting));

        return resulting;
    }

    private Experiment updateExperimentGapStop(int id, Experiment experiment, Experiment old, ExperimentRecord original) {
        List<Experiment.Population> populations = old.getPopulationsList();

        Predicate<Experiment.Population> listContains = population -> {
            for (Experiment.Population oldPopulation : populations) {
                if (population.getPlatformId().equals(oldPopulation.getPlatformId()))
                    return false;
            }
            return true;
        };

        List<Experiment.Population> newPopulations = experiment.getPopulationsList().stream().filter(listContains).collect(Collectors.toList());

        newPopulations.forEach(population -> {
            try {
                platformManager.publishTask(population.getPlatformId(), experiment).join();
                storePopulation(id, population);
            } catch (TaskOperationException e) {
                log.fatal(String.format("Error! Could not publish experiment %s on platfrom %s", experiment.getTitle(), population.getPlatformId()), e);
                e.printStackTrace();
            } catch (IllegalStateException | IllegalArgumentException e) {
                log.fatal("Error! Could not create experiment!" + e.getMessage());
            }
        });

        List<Experiment.Population> missingPopulations = experiment.getPopulationsList().stream().filter(population -> !listContains.test(population)).collect(Collectors.toList());

        //FIXME this is something like negativ gapfiller, this is not implemented yet.
        /*missingPopulations.forEach(failedPopulation -> {
            try {
                //platformManager.unpublishTask(failedPopulation.getPlatformId(),experiment).join();
                //TODO remove this population
            } catch (TaskOperationException e) {
              //TODO  log.fatal("Error! could not unpublish experiment from platform! "+ e.getMessage());
            }

        });*/

        return fetchExperiment(id);
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
        Experiment resulting;

        if (!old.getState().equals(Experiment.State.DRAFT)) {
            throw new IllegalStateException("When an experiment is running, only the state is allowed to be changed.");
        }

        ExperimentRecord experimentRecord = ExperimentTransformer.mergeProto(oldRecord, experiment);
        experimentRecord.setIdExperiment(id);

        Map<String, String> placeholders = experiment.getPlaceholders().size() > 0
                ? experiment.getPlaceholders()
                : old.getPlaceholders();

        String description = experiment.getDescription().equals("")
                ? old.getDescription()
                : experiment.getDescription();

        if (!Template.parse(description).keySet().equals(placeholders.keySet())) {
            throw new BadRequestException("Description and placeholder keys must match.");
        }

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

        // Update calibration records from experiment
        experiment.getPopulationsList().forEach(population -> {
            storePopulation(id, population);
        });

        if (!experiment.getPopulationsList().isEmpty()) {
            List<String> platforms = experiment.getPopulationsList().stream()
                    .map(Experiment.Population::getPlatformId)
                    .collect(Collectors.toList());
            experimentOperations.storeExperimentsPlatforms(platforms, id);
        }

        if (!Objects.equals(old.getAlgorithmTaskChooser().getName(), experimentRecord.getAlgorithmTaskChooser())) {
            algorithmsOperations.deleteChosenTaskChooserParams(id);
        }

        experiment.getAlgorithmTaskChooser().getParametersList().forEach(param -> algorithmsOperations.storeTaskChooserParam(id, param.getId(), param.getValue()));

        if (!Objects.equals(old.getAlgorithmQualityAnswer().getName(), experimentRecord.getAlgorithmQualityAnswer())) {
            algorithmsOperations.deleteChosenAnswerQualityParams(id);
        }

        experiment.getAlgorithmQualityAnswer().getParametersList().forEach(param -> algorithmsOperations.storeAnswerQualityParam(id, param.getId(), param.getValue()));

        if (!Objects.equals(old.getAlgorithmQualityRating().getName(), experimentRecord.getAlgorithmQualityRating())) {
            algorithmsOperations.deleteChosenRatingQualityParams(id);
        }

        experiment.getAlgorithmQualityRating().getParametersList().forEach(param -> algorithmsOperations.storeRatingQualityParam(id, param.getId(), param.getValue()));

        if (!experiment.getRatingOptionsList().isEmpty()) {
            experimentOperations.storeRatingOptions(experiment.getRatingOptionsList(), id);
        }

        //update the experiment itself
        experimentOperations.updateExperiment(experimentRecord);

        resulting = fetchExperiment(id);
        return resulting;
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
        Experiment resulting;

        //validate the only two possible changes
        if (!experiment.getState().equals(Experiment.State.PUBLISHED)
                && !experiment.getState().equals(Experiment.State.CREATIVE_STOPPED))
            throw new IllegalArgumentException("Only " + Experiment.State.PUBLISHED.name() +
                    " and " + Experiment.State.CREATIVE_STOPPED.name() +
                    " is allowed as state change");

        //validate its draft -> published
        if (experiment.getState() == Experiment.State.PUBLISHED && old.getState() != Experiment.State.DRAFT) {
            throw new IllegalArgumentException("Publish is only allowed for experiments in draft state.");
        }

        //validate its published -> creative_stopped
        if (experiment.getState() == Experiment.State.CREATIVE_STOPPED && old.getState() != Experiment.State.PUBLISHED) {
            throw new IllegalArgumentException("Creative stop is only allowed for published experiments.");
        }

        //check that there are enough datas for publish
        if (experiment.getState() == Experiment.State.PUBLISHED && experimentOperations.verifyExperimentForPublishing(id)) {
            throw new IllegalStateException("Experiment lacks information needed for publishing.");
        }

        //create the calibration for this experiment
        if (experiment.getState().equals(Experiment.State.PUBLISHED)) {
            calibrationOperations.createExperimentsCalibration(id, experiment);
        }

        //we are here if the state has changed and changed from draft to published
        if (experiment.getState() == Experiment.State.PUBLISHED) {
            //TODO start from populations
        }

        //check if we are not creative Stopped
        if (experiment.getState() == Experiment.State.CREATIVE_STOPPED) {
            //update db
            tasksOperations.getTasks(id).forEach(taskRecord -> {
                taskRecord.setStatus(TaskStatus.stopping);
                tasksOperations.updateTask(taskRecord);
            });
        }

        resulting = fetchExperiment(id);
        return resulting;
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
