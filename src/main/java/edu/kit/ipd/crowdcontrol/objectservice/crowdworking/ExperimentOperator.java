package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ExperimentsPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformStatusRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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


    private final EventManager eventManager;
    /**
     * Create a new operator class
     * @param platformManager
     * @param eventManager
     */
    public ExperimentOperator(PlatformManager platformManager,ExperimentFetcher experimentFetcher,ExperimentsPlatformOperations experimentsPlatformOperations,EventManager eventManager) {
        this.platformManager = platformManager;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.experimentsPlatformOperations = experimentsPlatformOperations;
        this.experimentFetcher = experimentFetcher;
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

        Set<ExperimentsPlatformStatusPlatformStatus> statuses = experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(experiment.getId());
         if(statuses.contains(ExperimentsPlatformStatusPlatformStatus.running)  && !statuses.contains(ExperimentsPlatformStatusPlatformStatus.shutdown)) {

            for (Experiment.Population population :
                    experiment.getPopulationsList()) {
                try {
                    platformManager.unpublishTask(population.getPlatformId(), experiment).join();
                } catch (PreActionException | CompletionException e) {
                    log.fatal("Failed to unpublish experiment " + experiment + " on platform " + population.getPlatformId(), e);
                }
            }

            experimentsPlatformOperations.setGlobalPlatformStatus(experiment, ExperimentsPlatformStatusPlatformStatus.shutdown);
            shutdownExperiment(experiment);
        }else if(!statuses.contains(ExperimentsPlatformStatusPlatformStatus.running)){
            log.info("Experiment "+experiment.getId()+" is not running ");
        }else {
            log.info("Experiment "+experiment.getId()+" is already shutting down");

        }
    }


    /**
     * Recovers experiments from shutdown-failure
     */
    public void recoverExperiments(){
        experimentsPlatformOperations.getExperimentsFailedDuringShutdown().forEach(
                (exp) -> recoverExperimentShutdown(exp.getIdExperiment())
        );
       /* experimentsPlatformOperations.getExperimentsFailedWhileRunning().forEach(
                (exp) -> //TODO include rescheduleAnswers(exp.getID()); here! (DuplicateChecker)
        );*/

    }

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


        if(passedMins >= 120){
           resumeShutdownExperiment(experiment,-1);
        }else {
           resumeShutdownExperiment(experiment,120-(int)passedMins);
        }

    }

    private void shutdownExperiment(Experiment experiment){
        resumeShutdownExperiment(experiment,120);
    }

    private void resumeShutdownExperiment(Experiment experiment, int remainingMins){
        ScheduledFuture scheduledFuture= scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                experimentsPlatformOperations.setGlobalPlatformStatus(experiment,ExperimentsPlatformStatusPlatformStatus.stopped);
                eventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experimentFetcher.fetchExperiment(experiment.getId())));
            }
        },remainingMins,TimeUnit.MINUTES);
    }

 /*   /**
     * Private class for non-blocking shutdown-process

    private class ShutdownRunner extends Thread{
        private Experiment experiment;
        private int minutesToShutdown = 120;

        public ShutdownRunner(Experiment experiment){
            this.experiment = experiment;
        }

        /**
         * Waits a specified amount of time until stopping the experiment and setting its state accordingly

        public void start(){
            try {
                TimeUnit.MINUTES.sleep(minutesToShutdown);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            experimentsPlatformOperations.setGlobalPlatformStatus(experiment,ExperimentsPlatformStatusPlatformStatus.stopped);
            eventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experimentFetcher.fetchExperiment(experiment.getId())));
        }
        /**
        * Waits the remaining time of the shutdown process until stopping the experiment and setting its state accordingly

         public void runRemaining(int minutesToShutdown){ //Used by a recover-method
            this.minutesToShutdown = minutesToShutdown;
            start();
        }
    } */
}
