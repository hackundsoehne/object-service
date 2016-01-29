package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.WorkerTransform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lucaskrauss at 28.01.2016
 *
 * The PaymentCalculator calculates the payment of all workers of an experiment.
 * The payment of a worker depends on the number of good (above the specified threshold) answers and good ratings.
 * If the experiment's base payment is greater than zero the worker's payment is increased by that value.
 */
public class PaymentCalculator {


    private AnswerRatingOperations ops;

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
    public Map<Worker, Integer> estimatePayment(Experiment experiment) {

        Map<Worker, Integer> map = new HashMap<>();
        int paymentBase = experiment.getPaymentBase();
        int paymentAnswer = experiment.getPaymentAnswer();
        int paymentRating = experiment.getPaymentRating();

        //Gets a all answers of a worker above the specified threshold
        Map<WorkerRecord,Set<AnswerRecord>> workerAnswerSet = ops.getGoodAnswersOfExperiment(experiment.getId(), 0); //TODO params

        //For all good answers of the worker, his salary get increased by the payment for an answer
        for (Map.Entry<WorkerRecord,Set<AnswerRecord>> entry : workerAnswerSet.entrySet()) {
            Worker worker = WorkerTransform.toProto(entry.getKey());

            if(!map.containsKey(worker)) {
                map.put(worker, 0);
            }

            for (int i = 0; i < entry.getValue().size(); i++) {
                map.put(worker, map.get(worker) + paymentAnswer);
            }
        }

        Map<WorkerRecord,Set<RatingRecord>> workerRatingSet = ops.getGoodRatingsOfExperiment(experiment.getId(), 0); //TODO ratings params

        //For all good ratings of the worker, his salary get increased by the payment for a rating
        for (Map.Entry<WorkerRecord,Set<RatingRecord>> entry : workerRatingSet.entrySet()) {
            Worker worker = WorkerTransform.toProto(entry.getKey());

            if(!map.containsKey(worker)) {
                map.put(worker, 0);
            }

            for (int i = 0; i < entry.getValue().size(); i++) {
                map.put(worker, map.get(worker) + paymentRating);
            }
        }


        //Now all workers participating in the experiment are present in the map and the base-payment can be added to their salary
        if(paymentBase > 0) {
            for (Map.Entry<Worker, Integer> entry : map.entrySet()) {
                entry.setValue(entry.getValue() + paymentBase);
            }
        }

        return map;
    }


}
