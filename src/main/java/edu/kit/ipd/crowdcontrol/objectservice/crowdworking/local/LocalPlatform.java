package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.local;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentificationComputation;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

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
    private final String url;
    /**
     * Create a new dummy platform with the given name
     * @param name the name is added as part of the id and to the name
     * @param url the url where the web view for the local-platform is located
     */
    public LocalPlatform(String name, String url) {
        this.name = name;
        this.url = url;
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
        return NAME + " " + name;
    }

    /**
     * Returns the ID of the platform.
     *
     * @return the string {@code "dummy"}
     * @see #ID
     */
    @Override
    public String getID() {
        return (ID + " " + name).replaceAll("[^a-z0-9]", "_");
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

    @Override
    public String getLink() {
        return url;
    }
}
