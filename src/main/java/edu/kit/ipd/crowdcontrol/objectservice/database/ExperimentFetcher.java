package edu.kit.ipd.crowdcontrol.objectservice.database;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformModeMode;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AlgorithmsTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstracts the code to get a full Experiment-Proto Object from the Database
 *
 * @author LeanderK
 * @author Marcel Hollerbach
 */
public class ExperimentFetcher {
    private final ExperimentOperations experimentOperations;
    private final ExperimentsPlatformOperations experimentsPlatformOperations;
    private final TagConstraintsOperations tagConstraintsOperations;
    private final AlgorithmOperations algorithmOperations;
    private final CalibrationOperations calibrationOperations;

    /**
     * Create a new Experiment Fetcher
     *  @param experimentOperations Operations to use
     * @param experimentsPlatformOperations Operations to use
     * @param tagConstraintsOperations Operations to use
     * @param algorithmOperations Operations to use
     * @param calibrationOperations Operations to use
     */
    public ExperimentFetcher(ExperimentOperations experimentOperations, ExperimentsPlatformOperations experimentsPlatformOperations, TagConstraintsOperations tagConstraintsOperations, AlgorithmOperations algorithmOperations, CalibrationOperations calibrationOperations) {
        this.experimentOperations = experimentOperations;
        this.experimentsPlatformOperations = experimentsPlatformOperations;
        this.tagConstraintsOperations = tagConstraintsOperations;
        this.algorithmOperations = algorithmOperations;
        this.calibrationOperations = calibrationOperations;
    }

    /**
     * Will take the id of an experiment and return the platform tree with
     * all published platforms with the according calibrations
     *
     * @param id the id of the experiment
     * @return returns a list of populations with a platform
     */
    public List<Experiment.Population> getPopulations(int id) {
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

        BiFunction<String, List<Calibration.Builder>, Experiment.Population.Builder> toPopulation = (name , calibrationsBuilders) -> {
            //normalize, multiple chosen answers from the same Calibration were multiple objects
            List<Calibration> calibrations = calibrationsBuilders.stream()
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
                    .setPlatformId(name)
                    .addAllCalibrations(calibrations);
        };

        Map<String, Experiment.Population.Builder> populations = experimentOperations.getCalibrations(id).entrySet().stream()
                .map(entry -> toPopulation.apply(entry.getKey().getPlatform(), entry.getValue().stream().map(toCalibration).collect(Collectors.toList())))
                .collect(Collectors.toMap(Experiment.Population.Builder::getPlatformId, Function.identity()));

        Function<ExperimentsPlatformModeMode, Experiment.Population.Task> mapModeTask = mode -> {
            switch (mode) {
                case normal: return Experiment.Population.Task.BOTH;
                case answer: return Experiment.Population.Task.ANSWER;
                case rating: return Experiment.Population.Task.RATING;
            }
            return Experiment.Population.Task.BOTH;
        };

        experimentsPlatformOperations.getActivePlatforms(id).entrySet()
                .forEach(entry -> {
                    if (!populations.containsKey(entry.getKey())) {
                        populations.put(entry.getKey(), Experiment.Population.newBuilder().setPlatformId(entry.getKey()));
                    }
                    populations.get(entry.getKey()).setTask(mapModeTask.apply(entry.getValue()));
                });

        return populations
                .entrySet().stream()
                .map(entry  -> entry.getValue().build())
                .collect(Collectors.toList());
    }

    /**
     * Fetches a new Experiment and all needed information from the db and creates a new Proto-Experiment Object.
     *
     * @param id database id to use
     *
     * @return A new experiment;
     * @throws NotFoundException for the case the experiment if not found
     */
    public Experiment fetchExperiment(int id) {
        ExperimentRecord experimentRecord = experimentOperations.getExperiment(id).orElseThrow(NotFoundException::new);
        Experiment.State state = experimentOperations.getExperimentState(id);
        List<RatingOptionExperimentRecord> ratingOptions = experimentOperations.getRatingOptions(id);
        List<TagRecord> tagRecords = tagConstraintsOperations.getTags(id);
        List<ConstraintRecord> constraintRecords = tagConstraintsOperations.getConstraints(id);
        List<Experiment.Population> populations = getPopulations(id);
        AlgorithmOption taskChooser = algorithmOperations.getTaskChooser(experimentRecord.getAlgorithmTaskChooser())
                .map(record -> AlgorithmsTransformer.toTaskChooserProto(record, algorithmOperations.getTaskChooserParams(
                        experimentRecord.getAlgorithmTaskChooser(), experimentRecord.getIdExperiment())))
                .orElse(null);
        AlgorithmOption answerQuality = algorithmOperations.getAnswerQualityRecord(experimentRecord.getAlgorithmQualityAnswer())
                .map(record -> AlgorithmsTransformer.toAnswerQualityProto(record, algorithmOperations.getAnswerQualityParams(
                        experimentRecord.getAlgorithmQualityAnswer(), experimentRecord.getIdExperiment())))
                .orElse(null);

        AlgorithmOption ratingQuality = algorithmOperations.getRatingQualityRecord(experimentRecord.getAlgorithmQualityRating())
                .map(record -> AlgorithmsTransformer.toRatingQualityProto(record, algorithmOperations.getRatingQualityParams(
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
}
