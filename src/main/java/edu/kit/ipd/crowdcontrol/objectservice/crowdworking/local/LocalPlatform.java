package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.local;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PaymentJob;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentificationComputation;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The LocalPlatform is a built-in Platform in CrowdControl.
 * @author LeanderK
 * @version 1.0
 */
public class LocalPlatform implements Platform {
    public static final String ID = "local";
    private final static String NAME = "CrowdControl Platform";
    private final String name;

    /**
     * Create a new dummy platform with the given name
     * @param name the name is added as part of the id and to the name
     */
    public LocalPlatform(String name) {
        this.name = name;
    }


    /**
     * if the Platform has his own payment service the implementation can return not none.
     *
     * @return The value to indicate if it supports self paying or not.
     */
    @Override
    public Optional<Payment> getPayment() {
        return Optional.empty();
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
    public CompletableFuture<JsonElement> publishTask(Experiment experiment) {
        return CompletableFuture.completedFuture(new JsonPrimitive("dummytask"));
    }

    /**
     * Unpublish the given id from the platform, after this call no worker should be able access the before published experiment.
     *
     * @return true on success, false or a exception if it failed
     */
    @Override
    public CompletableFuture<Boolean> unpublishTask(JsonElement data) {
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
}
