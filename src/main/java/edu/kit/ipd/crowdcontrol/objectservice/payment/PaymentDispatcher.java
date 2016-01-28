package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import rx.Observable;
import rx.Observer;

import java.util.Map;

/**
 * Created by lucaskrauss at 28.01.2016
 *
 *
 *
 *
 */
public class PaymentDispatcher implements Observer<ChangeEvent<Experiment>>{


    private Observable<ChangeEvent<Experiment>> observable = EventManager.EXPERIMENT_CHANGE.getObservable();
    private PlatformManager platformManager;
    private PaymentCalculator paymentCalc;

    public PaymentDispatcher(PlatformManager manager){
        observable.subscribe();
        this.platformManager = manager;
        this.paymentCalc = new PaymentCalculator();
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
    public void onNext(ChangeEvent<Experiment> experimentChangeEvent) {

        if(experimentChangeEvent.getNeww().getState() == Experiment.State.STOPPED
                && experimentChangeEvent.getOld().getState() == Experiment.State.STOPPING){

            int expId = experimentChangeEvent.getNeww().getId();
            dispatchPayment(expId);

        }
    }


    /**
     * Gets a map of workers and their corresponding payment via the PaymentCalculator.
     * Dispatches the salary of the workers via the PlatformManager
     * @param expId identification number of the finished experiment
     */
    private void dispatchPayment(int expId){
        Map<Worker,Integer> map;
        map = paymentCalc.estimatePayment(expId);

        map.forEach((Worker,Integer) -> platformManager.payWorker("",Worker,Integer));

    }





}
