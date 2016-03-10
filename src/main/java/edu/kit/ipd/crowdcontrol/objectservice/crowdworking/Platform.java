package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import com.google.gson.JsonElement;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 19.01.16.
 */
public interface Platform {
    /**
    * if the Platform has his own payment service the implementation can return not none.
    * @return the value to indicate if it supports self paying or not.
    */
    Optional<Payment> getPayment();

    /**
     * if the Platform has his own worker identification the interface can be returned here.
     * @return the value to indicate if it supports worker identification or not.
     */
    Optional<WorkerIdentificationComputation> getWorker();

    /**
     * Get the name of this platform
     * @return a String
     */
    String getName();

    /**
     * Returns the ID of the platform.
     * <p>
     * The String must be unique, must match {@code [a-z0-9_]+} and not change since it is the only way other services can
     * communicate with the platform.
     * @return a String
     */
    String getID();

    /**
     * Publish a passed experiment on the platform.
     * <p>
     * The database indexes on an String named identification, it will provide an speedup when used.
     * @param experiment the object which should be published
     * @return this should return all the data needed identify the published experiment later, returning null is not permitted!
     *         Or finish with a exception if the publishing failed.
     */
    CompletableFuture<JsonElement> publishTask(Experiment experiment);

    /**
     * Unpublish the given id from the platform, after this call no worker should be able access the before published experiment.
     * @param id the id of ExperimentsPlatform the before published experiment
     * @param data the stored data
     * @return true on success, false or a exception if it failed
     */
    CompletableFuture<Boolean> unpublishTask(int id, JsonElement data);

    /**
     * Returns if calibration questions can be asked on this platform
     * @return true or false
     */
    Boolean isCalibrationAllowed();
}
