package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatform implements Platform {
    private String workerServiceUrl;
    private String apiKey;
    private String apiUrl;
    private String name;
    private String projectID;
    private int idTask1;
    private int idTask2;

    /**
     * if the Platform has his own payment service the implementation can return not none.
     *
     * @return The value to indicate if it supports self paying or not.
     */
    public Optional<Payment> getPayment() {
        return Optional.empty();
    }

    /**
     * if the Platform has his own worker identification the interface can be returned here
     *
     * @return The value to indicate if it supports worker identification or not.
     */
    public Optional<WorkerIdentification> getWorker() {
        return Optional.of(this::identifyWorker);
    }

    private String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        return null;
    }

    /**
     * Get the name of this platform
     *
     * @return A unique String
     */
    public String getName() {
        return this.name;
    }

    private String publishTask() {
        return null;
    }

    /**
     * Publish a passed experiment on the platform
     *
     * @param experiment the object which should be published
     * @return This should return a unique string which is used to identify the published experiment later.
     * Or finish with a exception if the publishing failed.
     */
    public CompletableFuture<String> publishTask(Experiment experiment) {
        CompletableFuture<String> cf = new CompletableFuture<>();

        CompletableFuture.supplyAsync(this::publishTask);

        JsonNode jsonTask = new JsonNode("");

        jsonTask.getObject().append("project_id", projectID)
                .append("info", new JSONObject()
                        .append("url", workerServiceUrl)
                        .append("expID", experiment.getId())
                        .append("platformName", name)
                        .append("idTask1", idTask1)
                        .append("idTask2", idTask2)
                )
                .append("priority_0", 1);

        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(apiUrl + "task")
                    .header("Content-Type", "application/json")
                    .queryString("api_key", apiKey)
                    .body(jsonTask)
                    .asJson();
        } catch (UnirestException e) {
            // TODO error handling
            e.printStackTrace();
        }

        String taskID = response.getBody().getObject().getString("id")

        CompletableFu


        return cf;
    }

    /**
     * Unpublish the given id from the platform, after this call no worker should be able access the before published experiment.
     *
     * @param id The id of the before published experiment
     * @return true on success, false or a exception if it failed
     */
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return null;
    }

    /**
     * Update the published task, with the given id, to the parameters of experiment
     *
     * @param id         The id of the published Task
     * @param experiment The experiment with the new parameters
     * @return The new id or a Exception if the update failed.
     */
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return null;
    }

    /**
     * Returns if calibration questions can be asked on this platform
     *
     * @return true or false
     */
    public Boolean isCalibrationAllowed() {
        return null;
    }

    private void initProject() {
        HttpResponse<JsonNode> response = Unirest.get(apiUrl + "task")
                .queryString("api_key", apiKey)
                .queryString("project_id", projectID)
                .asJson();

        JsonNode json = response.getBody();

        if (json.isArray() && json.getArray().length() < 2) {

        }
    }

    private void initIdTasks() {
        JsonNode jsonTask = new JsonNode();
        jsonTask.getObject().append("project_id", projectID)
                .append("info", new JSONObject()
                        .append("idTask"))
                .append("priority_0", 0);

        HttpResponse<JsonNode> postResponse = Unirest.post(apiUrl + "task")
                .header("Content-Type", "application/json")
                .queryString("api_key", apiKey)
                .body(jsonTask)
                .asJson();
    }

    private void sdpublishTask(Experiment experiment) {


    }
}
