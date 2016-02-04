package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The dummy-platform represents a platform that does nothing. It is used to the the
 * functionality of the worker-service or any other service using the crowdworking-package.
 * This is useful for integration tests, but also used to test whether the worker-service is
 * responding to request once it is running or it has hung up.
 * @author LeanderK
 * @version 1.0
 */
public class DummyPlatform implements Platform, Payment {
    public final static String NAME = "dummy";
    /**
     * if the Platform has his own payment service the implementation can return not none.
     *
     * @return The value to indicate if it supports self paying or not.
     */
    @Override
    public Optional<Payment> getPayment() {
        return Optional.of(this);
    }

    /**
     * if the Platform has his own worker identification the interface can be returned here
     *
     * @return The value to indicate if it supports worker identification or not.
     */
    @Override
    public Optional<WorkerIdentification> getWorker() {
        return Optional.empty();
    }

    /**
     * Get the name of this platform
     *
     * @return A unique String
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Publish a passed experiment on the platform
     *
     * @param experiment the object which should be published
     * @return This should return a unique string which is used to identify the published experiment later.
     * Or finish with a exception if the publishing failed.
     */
    @Override
    public CompletableFuture<String> publishTask(Experiment experiment) {
        return CompletableFuture.completedFuture("dummytask");
    }

    /**
     * Unpublish the given id from the platform, after this call no worker should be able access the before published experiment.
     *
     * @param id The id of the before published experiment
     * @return true on success, false or a exception if it failed
     */
    @Override
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Update the published task, with the given id, to the parameters of experiment
     *
     * @param id         The id of the published Task
     * @param experiment The experiment with the new parameters
     * @return The new id or a Exception if the update failed.
     */
    @Override
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return CompletableFuture.completedFuture("dummytask");
    }

    /**
     * Returns if calibration questions can be asked on this platform
     *
     * @return true or false
     */
    @Override
    public Boolean isCalibrationAllowed() {
        return true;
    }

    /**
     * The passed list of paymentJobs contains all worker which have submitted a answer.
     * If the worker should not get payed because of bad ratings the amount should be smaller
     * than the basepayment of the experiment.
     *
     * @param id         id which was returned by publishTask
     * @param experiment experiment which is to be payed
     * @param paymentJob a list of tuples which maps each worker to a value
     * @return
     */
    @Override
    public CompletableFuture<Boolean> payExperiment(String id, Experiment experiment, List<PaymentJob> paymentJob) {
        return CompletableFuture.completedFuture(true);
    }
}
