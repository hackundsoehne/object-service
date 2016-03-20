package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformStatusRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.DuplicateChecker;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Presents starting and stopping experiment operations on populations
 *
 * @author Lucas Krauß
 * @author Marcel Hollerbach
 */
public class ExperimentOperator {
    private static final Logger log = LogManager.getLogger("ExperimentOperator");
    private final PlatformManager platformManager;
    private final ExperimentFetcher experimentFetcher;
    private final ExperimentsPlatformOperations experimentsPlatformOperations;
    private final ScheduledExecutorService scheduledExecutorService;
    private final DuplicateChecker duplicateChecker;
    private final int waitTimeInMin;

    private final EventManager eventManager;

    private final BlockingQueue<Experiment> unpublishingFailedQueue = new LinkedBlockingDeque<>();
    private final Thread retryShutdown = new Thread(this::retryShutdown);
    private boolean shutdown = false;

    /**
     * Create a new operator class
     * @param platformManager the platformManager to use
     * @param eventManager the eventManager to use
     * @param waitTimeInMin the time in minutes which is waited before the experiment is set to finished and payed;
     */
    public ExperimentOperator(PlatformManager platformManager, ExperimentFetcher experimentFetcher, ExperimentsPlatformOperations experimentsPlatformOperations,
                              EventManager eventManager, DuplicateChecker duplicateChecker, int waitTimeInMin) {
        this.waitTimeInMin = waitTimeInMin;
        this.platformManager = platformManager;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.experimentsPlatformOperations = experimentsPlatformOperations;
        this.experimentFetcher = experimentFetcher;
        this.duplicateChecker = duplicateChecker;
        this.eventManager = eventManager;
        recoverExperiments();
        retryShutdown.start();
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
        endExperiment(experiment, waitTimeInMin);
    }


    /**
     * End a given experiment
     *
     * @param experiment the experiment to end
     * @param waitingTime the duration of the shutdown-phase in minutes
     */
    private void endExperiment(Experiment experiment, int waitingTime) {
        Map<Integer, ExperimentsPlatformStatusPlatformStatus> statuses = experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(experiment.getId());

        Boolean shutdownInitiated = statuses.entrySet().stream()
                .filter(entry -> {
                    if (!entry.getValue().equals(ExperimentsPlatformStatusPlatformStatus.running)
                            || !entry.getValue().equals(ExperimentsPlatformStatusPlatformStatus.creative_stopping)) {
                        log.info(String.format("Can not shut down platform %s in state %s", entry.getKey(), entry.getValue().toString()));
                        return false;
                    }
                    return true;
                })
                .map(entry -> {
                    if (entry.getValue().equals(ExperimentsPlatformStatusPlatformStatus.shutdown)) {
                        return true;
                    }

                    Optional<ExperimentsPlatformRecord> opt = experimentsPlatformOperations.getExperimentsPlatform(entry.getKey());
                    if (!opt.isPresent()) {
                        log.error(String.format("unable to retrieve experimentsPlatform %s", entry.getKey()));
                        return true;
                    }
                    ExperimentsPlatformRecord experimentsPlatformRecord = opt.get();
                    boolean success = false;
                    try {
                        log.debug(String.format("unpublishing platform %s for experiment %s", entry.getKey(), experiment.getId()));
                        success = platformManager.unpublishTask(experimentsPlatformRecord.getPlatform(), experiment).join();
                    } catch (PreActionException | CompletionException e) {
                        experimentsPlatformOperations.setPlatformStatus(experiment.getId(), experimentsPlatformRecord.getPlatform(), ExperimentsPlatformStatusPlatformStatus.shutdown_failed);
                        log.error(String.format("unable to shut down platform %s for experiment %s", entry.getKey(), experiment.getId()));
                    }
                    if (success) {
                        log.debug(String.format("successfully unpublished platform %s for experiment %s", entry.getKey(), experiment.getId()));
                    }
                    return success;
                })
                .reduce(false, (b1, b2) -> b1 || b2);

        if (shutdownInitiated) {
            log.debug(String.format("shutdown successfully initiated for experiment %s", experiment.getId()));
            invokeShutdownPhase(experiment, waitingTime);
        } else {
            log.debug(String.format("shutdown was not successfully initiated for experiment %s, trying again later", experiment.getId()));
            unpublishingFailedQueue.add(experiment);
        }
    }

    /**
     * retries endExperiment for each item in unpublishingFailedQueue
     */
    private void retryShutdown() {
        while (!shutdown) {
            try {
                Experiment experiment = unpublishingFailedQueue.take();
                List<Experiment> retry = new ArrayList<>();
                retry.add(experiment);
                unpublishingFailedQueue.drainTo(retry);
                retry.forEach(this::endExperiment);
                //sleep for one minute before trying again
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.debug("interrupted", e);
            }
        }
    }


    /**
     * Recovers experiments from shutdown-failure
     */
    private void recoverExperiments(){
        experimentsPlatformOperations.getExperimentsFailedDuringShutdown().forEach(
                (exp) -> recoverExperimentShutdown(exp.getIdExperiment())
        );
        experimentsPlatformOperations.getRunningExperiments().forEach(
                (exp) -> duplicateChecker.rescheduleAnswersForDuplicateDetection(exp.getIdExperiment())
        );

    }

    /**
     * recovers the experiment by calculating the remaining wait time and invoking endExperiment
     * @param experimentID the primary key of the experiment
     */
    private void recoverExperimentShutdown(int experimentID){
        Experiment experiment = experimentFetcher.fetchExperiment(experimentID);
        List<ExperimentsPlatformStatusRecord> experimentsPlatformStatusRecords = experimentsPlatformOperations.getExperimentsPlatformStatusRecord(experimentID);
        long platformTime = 0;
        for (ExperimentsPlatformStatusRecord status: experimentsPlatformStatusRecords ) {
            if(status.getTimestamp().getTime() > platformTime){
                platformTime = status.getTimestamp().getTime();
            }
        }
        long passedTime = Timestamp.valueOf(LocalDateTime.now()).getTime()-platformTime;
        long passedMins = TimeUnit.MILLISECONDS.toMinutes(passedTime);

        if(passedMins >= waitTimeInMin){
            endExperiment(experiment,-1);
        }else {
            endExperiment(experiment,waitTimeInMin-(int)passedMins);
        }

    }

    /**
     * starts the shutdown-phase
     * @param experiment the primary key of the experiment
     * @param waitTime the minutes to wait
     * @return the generated {@link ScheduledFuture} waiting the waitTime to end finish the experiment
     */
    private ScheduledFuture invokeShutdownPhase(Experiment experiment, int waitTime){
        log.debug("scheduling shutdown for experiment {}.", experiment);
        ScheduledFuture scheduledFuture = scheduledExecutorService.schedule((Runnable) () -> {
            log.info("shutting down experiment {}.", experiment);
            experimentsPlatformOperations.setGlobalPlatformStatus(experiment,ExperimentsPlatformStatusPlatformStatus.finished);
            eventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experimentFetcher.fetchExperiment(experiment.getId())));
        },waitTime,TimeUnit.MINUTES);
        return scheduledFuture;
    }

    /**
     * stops the associated thread
     */
    public void stop() {
        shutdown = true;
        retryShutdown.interrupt();
    }

}
