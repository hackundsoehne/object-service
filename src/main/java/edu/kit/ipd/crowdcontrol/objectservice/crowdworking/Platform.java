package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 19.01.16.
 */
public interface Platform {
    /**
     * if the Platform has his own payment service the implementation can return not none.
     *
     * @return The value to indicate if it supports self paying or not.
     */
    Optional<Payment> getPayment();

    /**
     * if the Platform has his own worker identification the interface can be returned here
     *
     * @return The value to indicate if it supports worker identification or not.
     */
    Optional<WorkerIdentification> getWorker();

    /**
     * Get the name of this platform
     *
     * @return A unique String
     */
    String getName();

    /**
     * Publish a passed experiment on the platform
     *
     * @param experiment the object which should be published
     * @return This should return a unique string which is used to identify the published experiment later.
     * Or finish with a exception if the publishing failed.
     */
    CompletableFuture<String> publishTask(Experiment experiment);

    /**
     * Unpublish the given id from the platform, after this call no worker should be able access the before published experiment.
     *
     * @param id The id of the before published experiment
     * @return true on success, false or a exception if it failed
     */
    CompletableFuture<Boolean> unpublishTask(String id);

    /**
     * Update the published task, with the given id, to the parameters of experiment
     *
     * @param id         The id of the published Task
     * @param experiment The experiment with the new parameters
     * @return The new id or a Exception if the update failed.
     */
    CompletableFuture<String> updateTask(String id, Experiment experiment);

    /**
     * Returns if calibration questions can be asked on this platform
     *
     * @return true or false
     */
    Boolean isCalibrationAllowed();
}
