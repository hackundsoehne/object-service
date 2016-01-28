package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
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
    private final String workerServiceUrl;
    private final String apiKey;
    private final String apiUrl;
    private final String taskUrl;
    private final String name;
    private final String projectID;
    private final Boolean calibsAllowed;
    private String idTask1 = null;
    private String idTask2 = null;

    public PyBossaPlatform(String workerServiceUrl, String apiKey, String apiUrl, String name, String projectID, Boolean calibsAllowed) {
        this.workerServiceUrl = workerServiceUrl;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.name = name;
        this.projectID = projectID;
        this.calibsAllowed = calibsAllowed;

        taskUrl = apiUrl + "/task";
        initIdTasks();
    }

    private void initIdTasks() {
        if (!getIdTasks()) {
            createIdTasks();
        }
    }

    private boolean getIdTasks() {
        // request existing tasks for project
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(taskUrl)
                    .queryString("api_key", apiKey)
                    .queryString("project_id", projectID)
                    .asJson();
        } catch (UnirestException e) {
            throw new PlatformConnectionException(e);
        }

        // check for existent idTasks
        if (response.getStatus() == 200) {
            if (response.getBody().getArray().length() >= 2) {
                String[] ids = new String[2];

                // idTasks have to be the first two objects
                for (int i = 0; i < 1; i++) {
                    JSONObject task = response.getBody().getArray().getJSONObject(i);
                    if (task.isNull("info")) {
                        ids[i] = task.getString("id");
                    }
                }

                idTask1 = ids[0];
                idTask2 = ids[1];
            }
        } else {
            throw new PlatformTaskException(response.getBody().getObject()
                    .optString("exception_msg", "getIdTasks failed"));
        }

        return true;
    }

    /**
     * Creates 2 new idTasks with empty info object and assigns the IDs to idTask1 and idTask2
     */
    private void createIdTasks() {
        JsonNode jsonTask = new JsonNode("");
        jsonTask.getObject()
                .append("project_id", projectID)
                .append("priority_0", 0);

        String[] ids = new String[2];
        HttpResponse<JsonNode> response;
        for (int i = 0; i < 1; i++) {
            try {
                response = Unirest.post(taskUrl)
                        .header("Content-Type", "application/json")
                        .queryString("api_key", apiKey)
                        .body(jsonTask)
                        .asJson();
            } catch (UnirestException e) {
                throw new PlatformConnectionException(e);
            }

            if (response.getStatus() == 200) {
                ids[i] = response.getBody().getObject().getString("id");
            } else {
                throw new PlatformTaskException(response.getBody().getObject()
                        .optString("exception_msg", "createIdTasks failed"));
            }
        }

        idTask1 = ids[0];
        idTask2 = ids[1];
    }

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

    /**
     * Publish a passed experiment on the platform
     *
     * @param experiment the object which should be published
     * @return This should return a unique string which is used to identify the published experiment later.
     * Or finish with a exception if the publishing failed.
     */
    public CompletableFuture<String> publishTask(Experiment experiment) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonTask = new JsonNode("");

            jsonTask.getObject().append("project_id", projectID)
                    .append("info", new JSONObject()
                            .append("url", workerServiceUrl)
                            .append("expID", experiment.getId())
                            .append("platformName", name)
                            .append("idTask1", idTask1)
                            .append("idTask2", idTask2)
                    )
                    .append("priority_0", 1)
                    .append("n_answers", experiment.getNeededAnswers());

            HttpResponse<JsonNode> response;
            try {
                response = Unirest.post(taskUrl)
                        .header("Content-Type", "application/json")
                        .queryString("api_key", apiKey)
                        .body(jsonTask)
                        .asJson();
            } catch (UnirestException e) {
                throw new PlatformConnectionException(e);
            }

            String taskID = null;
            if (response.getStatus() == 200) {
                taskID = response.getBody().getObject().getString("id");
            } else {
                throw new PlatformTaskException(response.getBody().getObject()
                        .optString("exception_msg", "Publishing task failed"));
            }
            return taskID;
        });
    }

    /**
     * Unpublish the given id from the platform, after this call no worker should be able access the before published experiment.
     *
     * @param id The id of the before published experiment
     * @return true on success, false or a exception if it failed
     */
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return CompletableFuture.supplyAsync(() -> {
            HttpResponse<JsonNode> response;
            try {
                response = Unirest.delete(taskUrl + "/{taskId}")
                        .header("Content-Type", "application/json")
                        .routeParam("taskId", id)
                        .queryString("api_key", apiKey)
                        .asJson();
            } catch (UnirestException e) {
                throw new PlatformConnectionException(e);
            }

            // if response status is 204 the task was successfully deleted
            return response.getStatus() == 204;
        });
    }

    /**
     * Update the published task, with the given id, to the parameters of experiment
     *
     * @param id         The id of the published Task
     * @param experiment The experiment with the new parameters
     * @return The new id or a Exception if the update failed.
     */
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return CompletableFuture.supplyAsync(() -> {
            //build request body
            JsonNode jsonTask = new JsonNode("");
            jsonTask.getObject()
                    .append("info", new JSONObject()
                            .append("url", workerServiceUrl)
                            .append("expID", experiment.getId())
                            .append("platformName", name)
                            .append("idTask1", idTask1)
                            .append("idTask2", idTask2)
                    )
                    .append("n_answers", experiment.getNeededAnswers());

            HttpResponse<JsonNode> response;
            try {
                response = Unirest.put(taskUrl + "/{taskId}")
                        .header("Content-Type", "application/json")
                        .routeParam("taskId", id)
                        .queryString("api_key", apiKey)
                        .body(jsonTask)
                        .asJson();
            } catch (UnirestException e) {
                throw new PlatformConnectionException(e);
            }


            if (response.getStatus() == 200) {
                return id;
            } else {
                throw new PlatformTaskException(response.getBody().getObject()
                        .optString("exception_msg", "Updating task failed"));
            }
        });
    }

    /**
     * Returns if calibration questions can be asked on this platform
     *
     * @return true or false
     */
    public Boolean isCalibrationAllowed() {
        return this.calibsAllowed;
    }
}
