package edu.kit.ipd.crowdcontrol.objectservice.database;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformModeMode;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.CalibrationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Helperclass which abstracts the operations on Populations
 *
 * @author Marcel Hollerbach
 * @author LeanderK
 */
public class PopulationsHelper {
    private final ExperimentOperations experimentOperations;
    private final CalibrationOperations calibrationOperations;
    private final ExperimentsPlatformOperations experimentsPlatformOperations;

    /**
     * Creates a new instance with the given operations to use
     * @param experimentOperations operations to use
     * @param calibrationOperations operations to use
     * @param experimentsPlatformOperations operations to use
     */
    public PopulationsHelper(ExperimentOperations experimentOperations, CalibrationOperations calibrationOperations, ExperimentsPlatformOperations experimentsPlatformOperations) {
        this.experimentOperations = experimentOperations;
        this.calibrationOperations = calibrationOperations;
        this.experimentsPlatformOperations = experimentsPlatformOperations;
    }

    /**
     * Store the list of populations for the given experiment
     *
     * If the list is empty nothing is done, otherwise the old list is overridden by the new one
     *
     * @param experimentId id where to store the populations
     * @param populations list of populations to store in the experiment
     */
    public void storePopulations(int experimentId, List<Experiment.Population> populations) {
        if (populations.isEmpty())
            return;

        List<Experiment.Population> toStore = populations.stream()
                .filter(population -> !population.getPlatformId().isEmpty())
                .collect(Collectors.toList());

        List<String> platformsToStore = toStore.stream()
                .map(Experiment.Population::getPlatformId)
                .collect(Collectors.toList());
        experimentOperations.storeExperimentsPlatforms(platformsToStore, experimentId);

        BiConsumer<String, List<Calibration>> storeCalibrations = (platform, calibrations) -> {
            List<Integer> answerIDs = calibrations.stream()
                    .flatMap(calibration -> calibration.getAcceptedAnswersList().stream())
                    .map(Calibration.Answer::getId)
                    .collect(Collectors.toList());

            calibrationOperations.storeExperimentCalibrations(platform, answerIDs, experimentId);
        };

        toStore.forEach(population ->
                storeCalibrations.accept(population.getPlatformId(), population.getCalibrationsList()));
    }

    /**
     * Insert a single population into the list of populations of a experiment
     * @param experimentID id of the experiment
     * @param population population to save
     * @param mode the mode the population is in
     */
    public void insertPopulation(int experimentID, Experiment.Population population, ExperimentsPlatformModeMode mode) {
        experimentsPlatformOperations.insertPlatform(population.getPlatformId(), experimentID, mode);

        List<Integer> answerIDs = population.getCalibrationsList().stream()
                .flatMap(calibration -> calibration.getAcceptedAnswersList().stream())
                .map(Calibration.Answer::getId)
                .collect(Collectors.toList());

        calibrationOperations.storeExperimentCalibrations(population.getPlatformId(), answerIDs, experimentID);
    }
}
