package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 19.01.16.
 */
public interface Payment {
    /**
     *
     * The passed list of paymentJobs contains all worker which have submitted a answer.
     * If the worker should not get payed because of bad ratings the amount should be smaller
     * than the basepayment of the experiment.
     *
     * @param id id which was returned by publishTask
     * @param experiment experiment which is to be payed
     * @param paymentJob a list of tuples which maps each worker to a value
     * @return
     */
    CompletableFuture<Boolean> payExperiment(String id, Experiment experiment, List<PaymentJob> paymentJob);
}
