package edu.kit.ipd.crowdcontrol.objectservice.database;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AlgorithmOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.CalibrationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TagConstraintsOperations;
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
    private final TagConstraintsOperations tagConstraintsOperations;
    private final AlgorithmOperations algorithmOperations;
    private final CalibrationOperations calibrationOperations;

    /**
     * Create a new Experiment Fetcher
     *
     * @param experimentOperations Operations to use
     * @param tagConstraintsOperations Operations to use
     * @param algorithmOperations Operations to use
     * @param calibrationOperations Operations to use
     */
    public ExperimentFetcher(ExperimentOperations experimentOperations, TagConstraintsOperations tagConstraintsOperations, AlgorithmOperations algorithmOperations, CalibrationOperations calibrationOperations) {
        this.experimentOperations = experimentOperations;
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


        Map<String, List<Calibration.Builder>> populations = experimentOperations.getCalibrations(id).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getPlatform(),
                        entry -> entry.getValue().stream().map(toCalibration).collect(Collectors.toList())
                ));

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
