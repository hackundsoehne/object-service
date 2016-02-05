package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.IllegalWorkerSetException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PaymentJob;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.TaskOperationException;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Observer;

import java.util.stream.Collectors;

/**
 * Created by lucaskrauss at 28.01.2016
 * <p>
 * The PaymentDispatcher has a subscription to an EXPERIMENT_CHANGE-Observable.
 * On experiment-end, the PaymentDispatcher collects the payments per worker via the PaymentCalculator
 * and uses the results of that calculation to dispatch the payments to the PlatformManager.
 */
public class PaymentDispatcher implements Observer<ChangeEvent<Experiment>> {
    private final Logger log = LogManager.getLogger(PaymentDispatcher.class);
    private final Observable<Event<ChangeEvent<Experiment>>> observable = EventManager.EXPERIMENT_CHANGE.getObservable();
    private final PlatformManager platformManager;
    private final PaymentCalculator paymentCalc;


    /**
     * Constructor
     *
     * @param manager    PlatformManager being the target of the dispatching process
     * @param operations AnswerRatingOperations-class of the database-package. Enables db-calls
     */
    public PaymentDispatcher(PlatformManager manager, AnswerRatingOperations operations, WorkerOperations workerOperations) {
        observable.subscribe();
        this.platformManager = manager;
        this.paymentCalc = new PaymentCalculator(operations,workerOperations);
    }


    @Override
    public void onCompleted() {
        //NOP
    }

    @Override
    public void onError(Throwable e) {
        //NOP
    }


    /**
     * The onNext-method is called if an experiment has changed and that change has been communicated
     * via the EXPERIMENT_CHANGE-observable.
     * The PaymentDispatcher checks if the state of the experiment has changed from STOPPING to STOPPED.
     * In that case the payment is calculated (via PaymentCalculator) and dispatched to the PlatformManager.
     *
     * @param experimentChangeEvent representation of the changed experiment before and after the change
     */
    @Override
    public void onNext(ChangeEvent<Experiment> experimentChangeEvent) {
        if (experimentChangeEvent.getNeww().getState() == Experiment.State.STOPPED
                && experimentChangeEvent.getOld().getState() == Experiment.State.CREATIVE_STOPPED) {
            dispatchPayment(experimentChangeEvent.getNeww());
        }
    }


    /**
     * Gets a map of workers and their corresponding payment via the PaymentCalculator.
     * Dispatches the salary of the workers via the PlatformManager
     *
     * @param exp the finished experiment
     */
    private void dispatchPayment(Experiment exp) {
        paymentCalc.estimatePayment(exp).entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().getPlatform(),
                        Collectors.mapping(entry -> new PaymentJob(entry.getKey(), entry.getValue()), Collectors.toList())
                ))
                .forEach((platform, paymentJobs) -> {
                    try {
                        platformManager.payExperiment(platform, exp, paymentJobs);
                    } catch (TaskOperationException | IllegalWorkerSetException e) {
                        //TODO maybe notify researcher?
                        log.fatal(String.format("Unable to pay workers for platform %s", platform), e);
                    }
                });
    }


}
