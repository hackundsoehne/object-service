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
    public static final String ID = "dummy";
    private final static String NAME = "Dummy Platform";
    private final String name;

    /**
     * Create a new dummy platform with the given name
     * @param name the name is added as part of the id and to the name
     */
    public DummyPlatform(String name) {
        this.name = name;
    }


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
     * if the Platform has his own worker identification the interface can be returned here.
     * @return the value to indicate if it supports worker identification or not.
     */
    @Override
    public Optional<WorkerIdentificationComputation> getWorker() {
        return Optional.empty();
    }

    /**
     * Get the name of this platform
     *
     * @return the string {@code "Dummy Platform"}
     */
    @Override
    public String getName() {
        String appendix = "";
        if (name != null) {
            appendix = " " + name;
        }
        return NAME+appendix;
    }

    /**
     * Returns the ID of the platform.
     *
     * @return the string {@code "dummy"}
     * @see #ID
     */
    @Override
    public String getID() {
        String appendix = "";
        if (name != null) {
            appendix = name.toLowerCase();
        }
        return ID+appendix;
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
