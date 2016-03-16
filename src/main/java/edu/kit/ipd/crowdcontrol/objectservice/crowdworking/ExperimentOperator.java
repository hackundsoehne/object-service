package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

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
    private final EventManager eventManager;
    /**
     * Create a new operator class
     * @param platformManager
     * @param eventManager
     */
    public ExperimentOperator(PlatformManager platformManager, EventManager eventManager) {
        this.platformManager = platformManager;
        this.eventManager = eventManager;
    }

    /**
     * start a experiment on the configured platforms
     * @param experiment experiment to publish
     */
    public void startExperiment(Experiment experiment) {
        List<Experiment.Population> populations = experiment.getPopulationsList();
        List<Experiment.Population> successfulOps = new LinkedList<>();
        for (Experiment.Population population : populations) {
            try {
                if (!platformManager.publishTask(population.getPlatformId(), experiment).join()) {
                    break;
                }
            } catch (PreActionException | CompletionException e) {
                log.fatal("Failed to publish experiment "+experiment+" on platform "+population.getPlatformId(), e);
                break;
            }
            successfulOps.add(population);
        }

        if (successfulOps.size() != populations.size()) {
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

        eventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experiment.toBuilder().setState(Experiment.State.STOPPED).build()));
    }
}
