package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.google.common.primitives.Ints;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the support for the pybossa platform.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatform implements Platform {
    /**
     * IDTASK_COUNT is the number of idTasks that are used to get the worker's id
     */
    private static final int IDTASK_COUNT = 3;
    private final String workerServiceUrl;
    private final String name;
    private final int projectID;
    private final Boolean calibsAllowed;
    private int[] idTasks = new int[IDTASK_COUNT];
    private PyBossaRequests requests;

    /**
     * The implementation of the pybossa platform.
     * Call init after instantiation.
     * You need to setup a project on the specified pybossa platform before you can use this.
     *
     * @param workerServiceUrl the url of the worker-service
     * @param apiKey           the api key to access the pyboss api
     * @param apiUrl           the api of the url
     * @param name             the name of the platform
     * @param projectID        the project id
     * @param calibsAllowed    true if calibrations are allowed
     */
    public PyBossaPlatform(String workerServiceUrl, String apiKey, String apiUrl, String name, String projectID, Boolean calibsAllowed) {
        this.workerServiceUrl = workerServiceUrl;
        this.name = name;
        this.projectID = java.lang.Integer.parseInt(projectID);
        this.calibsAllowed = calibsAllowed;

        this.requests = new PyBossaRequests(apiUrl, this.projectID, apiKey);
    }

    /**
     * Initializes the pybossa idTasks. This makes requests to the pybossa platform
     */
    public void init() {
        initializeIdTasks();
    }

    @Override
    public Optional<Payment> getPayment() {
        return Optional.empty();
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
        return CompletableFuture.supplyAsync(() -> String.valueOf(requests.postTask(new JSONObject()
                .put("project_id", projectID)
                .put("info", new JSONObject()
                        .put("url", workerServiceUrl)
                        .put("expID", experiment.getId())
                                .put("platform", getID())
                        .put("idTasks", new JSONArray(idTasks))
                        .put("type", "experiment")
                        .put("paymentBase", experiment.getPaymentBase())
                        .put("paymentRating", experiment.getPaymentRating())
                        .put("paymentAnswer", experiment.getPaymentAnswer())
                        //pybossa doesn't support tags
                )
                .put("priority_0", 1)
                .put("n_answers", experiment.getNeededAnswers().getValue())))
        );
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return CompletableFuture.supplyAsync(() -> requests.deleteTask(id));
    }

    @Override
    @Deprecated
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return CompletableFuture.supplyAsync(() -> {
            JSONObject updatedTask = requests.updateTask(new JSONObject()
                    .put("info", new JSONObject()
                            .put("url", workerServiceUrl)
                            .put("expID", experiment.getId())
                            .put("platform", getID())
                            .put("idTasks", new JSONArray(idTasks))
                    )
                    .put("n_answers", experiment.getNeededAnswers()));
            return String.valueOf(updatedTask.getInt("id"));
        });
    }

    @Override
    public Optional<WorkerIdentification> getWorker() {
        return Optional.of(this::identifyWorker);
    }

    /**
     * IdentifyWorker takes the passed params and looks for a specified workerid, an idTask id and a code.
     * It then checks if there is one idTask that has been submitted by the worker before.
     * It compares the code saved in the taskRun, with the one passed in the params.
     * If the codes are equal, the workers identity is verified for this particular request.
     *
     * @param param the params passed from the worker ui
     * @return the worker's id
     * @throws UnidentifiedWorkerException if the worker cannot be identified
     */
    private String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        String[] emptyDefault = {""};
        String givenWorkerId = param.getOrDefault("workerId", emptyDefault)[0];
        String givenIdTask = param.getOrDefault("idTask", emptyDefault)[0];
        String givenCode = param.getOrDefault("code", emptyDefault)[0];

        String errorMessage = "";
        if (!givenWorkerId.isEmpty() && !givenCode.isEmpty() && !givenIdTask.isEmpty()) {
            // check if valid idTask
            if (Ints.contains(idTasks, java.lang.Integer.parseInt(givenIdTask))) {
                // check if givenCode matches saved random
                JSONArray taskRuns = requests.getTaskRuns(givenIdTask, givenWorkerId);
                // if all given values are valid there should only be one task run returned
                if (taskRuns.length() > 0) {
                    // delete task run anyway, so the idTask can be used again
                    requests.deleteTaskRun(taskRuns.getJSONObject(0).getInt("id"));
                    String savedCode = taskRuns.getJSONObject(0).getJSONObject("info").optString("code", "");
                    if (savedCode.equals(givenCode)) {
                        return givenWorkerId;
                    } else {
                        errorMessage = String.format("The identification code passed by worker %s, " +
                                "doesn't equal the code stored in the taskRun.", givenWorkerId);
                    }
                } else {
                    errorMessage = String.format("There was no taskRun found for idTask %s and worker %s.",
                            givenIdTask, givenWorkerId);
                }
            }
        } else {
            errorMessage = "Invalid parameters passed to PyBossaPlatform. Expected: " +
                    "workerId={id}&idTask={idTaskId}&code={theCode}";
        }
        throw new UnidentifiedWorkerException(errorMessage);
    }

    /**
     * Checks for existing idTasks and
     *
     * @throws PyBossaRequestException when the idtask could not be initialized
     */
    private void initializeIdTasks() {
        JSONArray allTasks = requests.getAllTasks();
        // idTasks should be the first objects
        for (int i = 0; i < IDTASK_COUNT; i++) {
            JSONObject task = allTasks.optJSONObject(i);
            if (task != null) {
                JSONObject info = task.optJSONObject("info");
                if (info.optString("type", "none").equals("idTask")) {
                    idTasks[i] = task.getInt("id");
                } else {
                    throw new PyBossaRequestException("Could not initialize tasks for platform "
                            + this.getName() + ": Invalid idTasks.");
                }
            } else {
                idTasks[i] = createIdTask();
            }
        }
    }

    /**
     * Creates new idTasks with empty info object and assigns the IDs to the idTasks
     */
    private int createIdTask() {
        JSONObject jsonTask = new JSONObject()
                .put("project_id", projectID)
                .put("priority_0", 0)
                .put("info", new JSONObject()
                        .put("type", "idTask"));
        return requests.postTask(jsonTask);
    }
}
