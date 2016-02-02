package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import rx.Observable;
import rx.Observer;

import java.util.Map;

/**
 * Created by lucaskrauss at 28.01.2016
 * <p>
 * The PaymentDispatcher has a subscription to an EXPERIMENT_CHANGE-Observable.
 * On experiment-end, the PaymentDispatcher collects the payments per worker via the PaymentCalculator
 * and uses the results of that calculation to dispatch the payments to the PlatformManager.
 */
public class PaymentDispatcher implements Observer<ChangeEvent<Experiment>> {


    private Observable<Event<ChangeEvent<Experiment>>> observable = EventManager.EXPERIMENT_CHANGE.getObservable();
    private PlatformManager platformManager;
    private PaymentCalculator paymentCalc;


    /**
     * Constructor
     *
     * @param manager    PlatformManager being the target of the dispatching process
     * @param operations AnswerRatingOperations-class of the database-package. Enables db-calls
     */
    public PaymentDispatcher(PlatformManager manager, AnswerRatingOperations operations) {
        observable.subscribe();
        this.platformManager = manager;
        this.paymentCalc = new PaymentCalculator(operations);
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
        Map<Worker, Integer> map;
        map = paymentCalc.estimatePayment(exp);

        map.forEach((Worker, Integer) -> platformManager.payWorker("", Worker, Integer));

    }


}
