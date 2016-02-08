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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatform implements Platform {
    /**
     * IDTASK_COUNT is the number of idTasks that are used to get the worker's id
     */
    private static final int IDTASK_COUNT = 2;
    private final String workerServiceUrl;
    private final String apiKey;
    private final String apiUrl;
    private final String taskUrl;
    private final String name;
    private final int projectID;
    private final Boolean calibsAllowed;
    private int[] idTasks = new int[IDTASK_COUNT];
    private PyBossaRequests requests;


    public PyBossaPlatform(String workerServiceUrl, String apiKey, String apiUrl, String name, int projectID, Boolean calibsAllowed) {
        this.workerServiceUrl = workerServiceUrl;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.name = name;
        this.projectID = projectID;
        this.calibsAllowed = calibsAllowed;

        taskUrl = apiUrl + "/task";
        this.requests = new PyBossaRequests(taskUrl, projectID, apiKey);

    }

    /**
     * Initializes the pybossa idTasks. This makes requests to the pybossa platform
     */
    public void init() {
        if (!getIdTasks()) {
            createIdTasks();
        }
    }

    @Override
    public Optional<Payment> getPayment() {
        return Optional.empty();
    }

    @Override
    public Optional<WorkerIdentification> getWorker() {
        return Optional.of(this::identifyWorker);
    }

    private String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        String givenId = param.get("id")[0];
        String givenIdTaskString = param.get("idTask")[0];
        String givenRandom = param.get("code")[0];

        if (givenId.isEmpty() || givenRandom.isEmpty() || givenIdTaskString.isEmpty()) {
            throw new UnidentifiedWorkerException();
        }

        int givenIdTask = Integer.valueOf(givenIdTaskString);

        // check if valid idTask
        if (Arrays.asList(idTasks).contains(givenIdTask)) {
            // check if givenRandom matches saved random
            HttpResponse<JsonNode> response;
            try {
                response = Unirest.get(apiUrl + "/taskrun")
                        .queryString("api_key", apiKey)
                        .queryString("task_id", givenIdTask)
                        .queryString("user_id", givenId)
                        .asJson();
            } catch (UnirestException e) {
                throw new PyBossaRequestException("identifyWorker failed on platform %s for worker %s", e);
            }

            JSONArray taskRuns = response.getBody().getArray();
            // if all given values are valid there is only one task run returned
            if (taskRuns.length() == 1) {
                String savedRandom = taskRuns.getJSONObject(0).getJSONObject("info").optString("random", "");
                if (!savedRandom.equals(givenRandom)) {
                    throw new UnidentifiedWorkerException();
                }
            }
            // delete task run anyway
            deleteTaskRun(Integer.toString(taskRuns.getJSONObject(0).getInt("id")));
        } else {
            throw new UnidentifiedWorkerException();
        }
        return null;
    }

    @Override
    public String getName() {
        return "Pybossa " + name;
    }

    @Override
    public String getID() {
        return "pybossa" + name.toLowerCase();
    }

    @Override
    public Boolean isCalibrationAllowed() {
        return this.calibsAllowed;
    }

    @Override
    public CompletableFuture<String> publishTask(Experiment experiment) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonTask = new JsonNode("");

            jsonTask.getObject()
                    .put("project_id", projectID)
                    .put("info", new JSONObject()
                            .put("url", workerServiceUrl)
                            .put("expID", experiment.getId())
                            .put("platformName", name)
                            .put("idTasks", new JSONArray(idTasks))
                    )
                    .put("priority_0", 1)
                    .put("n_answers", experiment.getNeededAnswers().getValue());

            HttpResponse<JsonNode> response;
            try {
                response = Unirest.post(taskUrl)
                        .header("Content-Type", "application/json")
                        .queryString("api_key", apiKey)
                        .body(jsonTask)
                        .asJson();
            } catch (UnirestException e) {
                throw new PyBossaRequestException(e);
            }

            String taskId = null;
            if (response.getStatus() == 200) {
                taskId = String.valueOf(response.getBody().getObject().getInt("id"));
            } else {
                throw new PyBossaRequestException(response.getBody().getObject()
                        .optString("exception_msg", "Publishing task failed"));
            }
            return taskId;
        });
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return CompletableFuture.supplyAsync(() -> requests.deleteTask(id));
    }

    @Override
    @Deprecated
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return CompletableFuture.supplyAsync(() -> {
            //build request body
            JsonNode jsonTask = new JsonNode("");
            jsonTask.getObject()
                    .put("info", new JSONObject()
                            .put("url", workerServiceUrl)
                            .put("expID", experiment.getId())
                            .put("platformName", name)
                            .put("idTasks", new JSONArray(idTasks))
                    )
                    .put("n_answers", experiment.getNeededAnswers());

            HttpResponse<JsonNode> response;
            try {
                response = Unirest.put(taskUrl + "/{taskId}")
                        .header("Content-Type", "application/json")
                        .routeParam("taskId", id)
                        .queryString("api_key", apiKey)
                        .body(jsonTask)
                        .asJson();
            } catch (UnirestException e) {
                throw new PyBossaRequestException(e);
            }


            if (response.getStatus() == 200) {
                return id;
            } else {
                throw new PyBossaRequestException(response.getBody().getObject()
                        .optString("exception_msg", "Updating task failed"));
            }
        });
    }

    /**
     * Checks for existing idTasks
     *
     * @return true if idTasks are present in the current project, else false
     */
    private boolean getIdTasks() {
        // request existing tasks for project
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(taskUrl)
                    .queryString("api_key", apiKey)
                    .queryString("project_id", Integer.toString(projectID))
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }

        // check for existent idTasks
        if (response.getStatus() == 200) {
            if (response.getBody().getArray().length() == 0) {
                return false;
            } else {
                // idTasks have to be the first two objects
                for (int i = 0; i < IDTASK_COUNT; i++) {
                    JSONObject task = response.getBody().getArray().optJSONObject(i);
                    if (task != null && task.isNull("info")) {
                        idTasks[i] = task.getInt("id");
                    } else {
                        throw new PyBossaRequestException("Could not initialize tasks for platform "
                                + this.getName() + ": Invalid idTasks.");
                    }
                }
            }
        } else {
            throw new PyBossaRequestException(response.getBody().getObject()
                    .optString("exception_msg", "getIdTasks failed"));
        }
        return true;
    }

    /**
     * Creates 2 new idTasks with empty info object and assigns the IDs to the idTasks
     */
    private void createIdTasks() {
        JsonNode jsonTask = new JsonNode(null);
        jsonTask.getObject()
                .put("project_id", projectID)
                .put("priority_0", 0);

        HttpResponse<JsonNode> response;
        for (int i = 0; i < IDTASK_COUNT; i++) {
            try {
                response = Unirest.post(taskUrl)
                        .header("Content-Type", "application/json")
                        .queryString("api_key", apiKey)
                        .body(jsonTask)
                        .asJson();
            } catch (UnirestException e) {
                throw new PyBossaRequestException("Could not connect to " + getID(), e);
            }

            if (response.getStatus() == 200) {
                idTasks[i] = response.getBody().getObject().getInt("id");
            } else {
                throw new RuntimeException(response.getBody().getObject()
                        .optString("exception_msg", "createIdTasks failed"));
            }
        }
    }

    private void deleteTaskRun(String taskRunId) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.delete(apiUrl + "/taskrun/{id}")
                    .queryString("api_key", apiKey)
                    .routeParam("id", taskRunId)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(String.format("Connection failed for platform %s.", getID()), e);
        }
        if (response.getStatus() != 204) {
            throw new PyBossaRequestException(String.format("Taskrun with id %s could not be deleted", taskRunId));
        }
    }

    private void deleteAllTaskRunsForTask(String task) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(apiUrl + "/taskrun")
                    .queryString("api_key", apiKey)
                    .queryString("task_id", task)

                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(String.format("Connection failed for platform %s.", getID()), e);
        }
        JSONArray taskRuns = response.getBody().getArray();

        for (int i = 0; i < taskRuns.length() - 1; i++) {
            try {
                Unirest.delete(apiUrl + "/taskrun/{id}")
                        .queryString("api_key", apiKey)
                        .routeParam("id", Integer.toString(taskRuns.getJSONObject(i).getInt("id")))
                        .asJson();
            } catch (UnirestException e) {
                throw new PyBossaRequestException(e);
            }
        }
    }
}
