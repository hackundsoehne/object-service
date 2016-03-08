package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.stream.Collectors;

/**
 * Created by lucaskrauss at 28.01.2016
 * <p>
 * The PaymentDispatcher has a subscription to an EXPERIMENT_CHANGE-Observable.
 * On experiment-end, the PaymentDispatcher collects the payments per worker via the PaymentCalculator
 * and uses the results of that calculation to dispatch the payments to the PlatformManager.
 */
public class PaymentDispatcher  {

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
        observable.subscribe(expChangeEvent -> {
            if(expChangeEvent.getData().getOld().getState() != Experiment.State.STOPPED
                    && expChangeEvent.getData().getNeww().getState() == Experiment.State.STOPPED){
                dispatchPayment(expChangeEvent.getData().getNeww());
            }
        });
        this.platformManager = manager;
        this.paymentCalc = new PaymentCalculator(operations,workerOperations);
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
                        //TODO props to the guy which missed the .join          ^^^^^^^^
                    } catch (PreActionException e) {
                        log.fatal("Payment of experiment "+exp+" on platform "+platform+" could not take place!", e.getCause());
                    }
                });
    }


}
