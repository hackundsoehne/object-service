package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.TaskOperationException;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventObservable;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Observer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucaskrauss at 05.02.2016
 */
public class ExperimentController implements Observer<Event<Experiment>> {

    private final Logger log = LogManager.getLogger(ExperimentController.class);
    private final Observable<Event<Experiment>> observable = EventManager.EXPERIMENT_CREATE.getObservable();
    private final EventObservable<ChangeEvent<Experiment>> endExpObservable = EventManager.EXPERIMENT_CHANGE;
    private final PlatformManager platformManager;

    public ExperimentController(PlatformManager manager) {
        platformManager = manager;
        observable.subscribe();

    }


    /**
     * Starts the experiment by publishing it on the participating platforms.
     * If an error occurs during the publishing, all already created tasks for this experiment will be undone
     *
     * @param experiment to be started
     */
    private void startExperiment(Experiment experiment) {
        Queue<String> successfulPlatforms = new LinkedList<>();
        for (int i = 0; i < experiment.getPopulationsCount(); i++) {
            try {
                platformManager.publishTask(experiment.getPopulations(i).getPlatformId(), experiment);
                successfulPlatforms.add(experiment.getPopulations(i).getPlatformId());
            } catch (TaskOperationException e1) {
                //could not create task
                log.fatal(String.format("Error! Could not create experiment on platform %s!", experiment.getPopulations(i).getPlatformId()), e1);
                unpublishExperiment(experiment, successfulPlatforms);
            } catch (IllegalStateException | IllegalArgumentException e2) {
                log.fatal("Error! Could not create experiment! " + e2.getMessage());
                unpublishExperiment(experiment, successfulPlatforms);
            }
        }

        //ChangeEvent mit State.INVALID ?


    }

    /**
     * Unpublishes an experiment from all platforms it is meant to be active on.
     * This method is only called if the initialization of the experiment has failed on one
     * of its platforms.
     *
     * @param experiment          which is going to be unpublished
     * @param successfulPlatforms all platforms the experiment already was published on
     */
    private void unpublishExperiment(Experiment experiment, Queue<String> successfulPlatforms) {

        while (!successfulPlatforms.isEmpty()) {
            try {
                platformManager.unpublishTask(successfulPlatforms.remove(), experiment);
            } catch (TaskOperationException e1) {
                log.fatal("Error! Could not unpublish experiment from platform! " + e1.getMessage());
            }
        }
    }


    /**
     * Unpublished the experiment from all its platform. Waits until the time for giving answers and/or ratings
     * on the platforms (specified in the config-file) has run out. The experiment's state is set to STOPPED and a
     * matching event is emitted.
     *
     * @param experiment which is to be ended.
     */
    public void endExperiment(Experiment experiment) {

        for (int i = 0; i < experiment.getPopulationsCount(); i++) {
            try {
                platformManager.unpublishTask(experiment.getPopulations(i).getPlatformId(), experiment);
            } catch (TaskOperationException e1) {
                log.fatal(String.format("Error! Cannot unpublish experiment from platform %s!", experiment.getPopulations(i).getPlatformId()), e1);
            }
        }
        //wait for crowd platform time out
        try {
            TimeUnit.HOURS.sleep(2);  //TODO get time from config
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Experiment newExperiment = experiment.toBuilder().setState(Experiment.State.STOPPED).build();
        //notify all observers
        endExpObservable.emit(new ChangeEvent<Experiment>(experiment, newExperiment));
    }

    @Override
    public void onCompleted() {
        //NOP
    }

    @Override
    public void onError(Throwable e) {
        //NOP
    }

    @Override
    public void onNext(Event<Experiment> experimentEvent) {
        startExperiment(experimentEvent.getData());

    }


}
