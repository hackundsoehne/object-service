package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.WorkerTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lucaskrauss at 28.01.2016
 *
 * The PaymentCalculator calculates the payment of all workers of an experiment.
 * The payment of a worker depends on the number of good (above the specified threshold) answers and good ratings.
 * If the experiment's base payment is greater than zero the worker's payment is increased by that value.
 */
public class PaymentCalculator {


    private final  AnswerRatingOperations ops;

    /**
     * Constructor
     * @param ops AnswerRatingOperation which enable database-access
     */
    public PaymentCalculator(AnswerRatingOperations ops) {
        this.ops = ops;

    }


    /**
     * Checks for all workers of the experiment the number of good answers and ratings.
     * Depending on the estimated amount of answers/ratings their salary is increased by (a multiple of) the payment
     * for an answer or/and (a multiple of) the payment for a rating.
     * Furthermore if the experiment's base-payment is greater than 0 each worker's salary is increased by that value
     * as well.
     * @param experiment representation of the experiment whose workers will have their payment estimated
     * @return a mapping of workers to their salary
     */
    public Map<Worker, Integer> estimatePayment(Experiment experiment)throws IllegalArgumentException {


        Map<Worker, Integer> map = new HashMap<>();
        int paymentBase = experiment.getPaymentBase();
        int paymentAnswer = experiment.getPaymentAnswer();
        int paymentRating = experiment.getPaymentRating();

        if(paymentAnswer < 0 || paymentBase < 0 || paymentRating < 0){
            throw new IllegalArgumentException("Error in "+this.getClass()+"! Payment-values must not be less than zero!");
        }

        //Gets number of all answers of a worker above the specified threshold
        Map<WorkerRecord,Integer> workerAnswerMap = ops.getNumOfGoodAnswersOfExperiment(experiment.getId(), 0); //TODO params

        //For all good answers of the worker, his salary get increased by the payment for an answer
        for (Map.Entry<WorkerRecord,Integer> entry :workerAnswerMap.entrySet()) {
            Worker worker = WorkerTransformer.toProto(entry.getKey());
            map.put(worker, (entry.getValue()* paymentAnswer));

        }

        Map<WorkerRecord, Integer> workerRatingMap = ops.getNumOfGoodRatingsOfExperiment(experiment.getId(), 0); //TODO ratings params

        //For all good ratings of the worker, his salary get increased by the payment for a rating
        for (Map.Entry<WorkerRecord, Integer> entry : workerRatingMap.entrySet()) {
            Worker worker = WorkerTransformer.toProto(entry.getKey());
            map.put(worker,map.get(worker) + (entry.getValue() * paymentRating));
        }


        //Now all workers participating in the experiment are present in the map and the base-payment can be added to their salary
        map.forEach((worker,payment) -> map.put(worker,payment+paymentBase));


        return map;
    }
}
