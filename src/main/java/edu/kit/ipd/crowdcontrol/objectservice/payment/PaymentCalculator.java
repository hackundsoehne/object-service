package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lucaskrauss at 28.01.2016
 */
public class PaymentCalculator {


    private AnswerRatingOperations ops;


    public PaymentCalculator(AnswerRatingOperations ops) {
        this.ops = ops;

    }

    public Map<Worker, Integer> estimatePayment(Experiment experiment) {

        Map<Worker, Integer> map = new HashMap<>();
        int paymentBase = experiment.getPaymentBase();
        int paymentAnswer = experiment.getPaymentAnswer();
        int paymentRating = experiment.getPaymentRating();


        return null;
    }


}
