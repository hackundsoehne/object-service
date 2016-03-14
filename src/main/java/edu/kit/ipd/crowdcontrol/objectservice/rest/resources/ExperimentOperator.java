package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PreActionException;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * Presents starting and stopping experiment operations on populations
 *
 * @author Lucas Krauß
 * @author Marcel Hollerbach
 */
public class ExperimentOperator {
    private static final Logger log = LogManager.getLogger("ExperimentOperator");
    private final PlatformManager platformManager;

    /**
     * Create a new operator class
     * @param platformManager
     */
    public ExperimentOperator(PlatformManager platformManager) {
        this.platformManager = platformManager;
    }

    /**
     * start a experiment on the configured platforms
     * @param experiment experiment to publish
     */
    public void startExperiment(Experiment experiment) {
        List<Experiment.Population> successfulOps = new LinkedList<>();
        for (Experiment.Population population :
                experiment.getPopulationsList()) {
            try {
                platformManager.publishTask(population.getPlatformId(), experiment).join();
                successfulOps.add(population);

            } catch (PreActionException | CompletionException e) {
                log.fatal("Failed to publish experiment "+experiment+" on platform "+population.getPlatformId(), e);
            }
        }

        if (successfulOps.size() != experiment.getPopulationsList().size()) {
            for (Experiment.Population population :
                    successfulOps) {
                try {
                    platformManager.unpublishTask(population.getPlatformId(), experiment).join();
                } catch (PreActionException | CompletionException e) {
                    log.fatal("Failed to publish experiment "+experiment+" on platform "+population.getPlatformId(), e);
                }
            }
        }
    }

    /**
     * End a given experiment
     *
     * @param experiment the experiment to end
     */
    public void endExperiment(Experiment experiment) {
        for (Experiment.Population population :
                experiment.getPopulationsList()) {
            try {
                platformManager.unpublishTask(population.getPlatformId(), experiment).join();
            } catch (PreActionException | CompletionException e) {
                log.fatal("Failed to publish experiment " + experiment + " on platform " + population.getPlatformId(), e);
            }
        }

        try { //TODO Introduce a more elaborate solution - W. Churchill
            TimeUnit.HOURS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experiment.toBuilder().setState(Experiment.State.STOPPED).build()));
    }
}
