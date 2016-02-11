package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

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
 * This class implements the support for the pybossa platform.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatform implements Platform {
    /**
     * IDTASK_COUNT is the number of idTasks that are used to get the worker's id
     */
    private static final int IDTASK_COUNT = 2;
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
        if (!getIdTasks()) {
            createIdTasks();
        }
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
                        .put("platformName", name)
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
                            .put("platformName", name)
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
        String givenWorkerId = param.get("id")[0];
        String givenIdTask = param.get("idTask")[0];
        String givenCode = param.get("code")[0];

        if (givenWorkerId.isEmpty() || givenCode.isEmpty() || givenIdTask.isEmpty()) {
            // check if valid idTask
            if (Arrays.asList(idTasks).contains(givenIdTask)) {
                // check if givenCode matches saved random
                JSONArray taskRuns = requests.getTaskRuns(givenIdTask, givenWorkerId);
                // if all given values are valid there should only be one task run returned
                if (taskRuns.length() == 1) {
                    String savedCode = taskRuns.getJSONObject(0).getJSONObject("info").optString("code", "");
                    if (savedCode.equals(givenCode)) {
                        return givenWorkerId;
                    }
                }
                // delete task run anyway
                requests.deleteTaskRun(taskRuns.getJSONObject(0).getInt("id"));
            }
        }
        throw new UnidentifiedWorkerException();
    }

    /**
     * Checks for existing idTasks
     *
     * @return true if idTasks are present in the current project, else false
     */
    private boolean getIdTasks() {
        JSONArray allTasks = requests.getAllTasks();
        // idTasks have to be the first two objects
        if (allTasks.length() <= IDTASK_COUNT) {
            return false;
        }
        for (int i = 0; i < IDTASK_COUNT; i++) {
            JSONObject task = allTasks.optJSONObject(i);
            if (task != null) {
                JSONObject info = task.optJSONObject("info");
                if (info.optString("type", "none").equals("idTask")) {
                    idTasks[i] = task.getInt("id");
                }
            } else {
                throw new PyBossaRequestException("Could not initialize tasks for platform "
                        + this.getName() + ": Invalid idTasks.");
            }
        }
        return true;
    }

    /**
     * Creates 2 new idTasks with empty info object and assigns the IDs to the idTasks
     */
    private void createIdTasks() {
        JSONObject jsonTask = new JSONObject()
                .put("project_id", projectID)
                .put("priority_0", 0)
                .put("info", new JSONObject()
                        .put("type", "idTask"));

        for (int i = 0; i < IDTASK_COUNT; i++) {
            idTasks[i] = requests.postTask(jsonTask);
        }
    }
}
