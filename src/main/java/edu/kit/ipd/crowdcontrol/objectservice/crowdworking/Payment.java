package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import com.google.gson.JsonElement;
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
     * @param id id of the ExperimentsPlatformRecord
     * @param data the data associated with the ExperimentsPlatformRecord
     * @param experiment experiment which is to be payed
     * @param paymentJob a list of tuples which maps each worker to a value
     * @return A Completable future which completes once the list of payments is submitted and done
     */
    CompletableFuture<Boolean> payExperiment(int id, JsonElement data, Experiment experiment, List<PaymentJob> paymentJob);

    /**
     * Indicates which currency is used for payment of the worker
     *
     * Code must be ISO 4217 conform.
     * For more details: https://de.wikipedia.org/wiki/ISO_4217
     *
     * @return code of the returned currency
     */
    int getCurrency();
}
