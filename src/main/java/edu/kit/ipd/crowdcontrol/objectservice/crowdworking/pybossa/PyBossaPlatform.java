package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.google.common.primitives.Ints;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
    private final String workerUiPublicUrl;
    private final String baseUrl;
    private final String name;
    private final int projectID;
    private final Boolean calibsAllowed;
    private String workerUiLocalUrl;
    private String projectShortName;
    private int[] idTasks = new int[IDTASK_COUNT];
    private PyBossaRequests requests;
    private MessageDigest messageDigest;

    /**
     * The implementation of the pybossa platform.
     * Call init after instantiation.
     * You need to setup a project on the specified pybossa platform before you can use this.
     *
     * @param workerServiceUrl the url of the worker-service
     * @param workerUiUrl      the url of the worker-ui
     * @param apiKey           the api key to access the pyboss api
     * @param baseUrl          the platform base url
     * @param name             the name of the platform
     * @param projectID        the project id
     * @param calibsAllowed    true if calibrations are allowed
     */
    public PyBossaPlatform(String workerServiceUrl, String workerUiUrl, String apiKey, String baseUrl,
                           String name, String projectID, Boolean calibsAllowed) {
        this.workerServiceUrl = workerServiceUrl;
        this.workerUiPublicUrl = workerUiUrl;
        this.workerUiLocalUrl = workerUiUrl;
        this.baseUrl = baseUrl;
        this.name = name;
        this.projectID = java.lang.Integer.parseInt(projectID);
        this.calibsAllowed = calibsAllowed;
        this.requests = new PyBossaRequests(baseUrl + "/api", this.projectID, apiKey);

        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // it's unlikely that this exception will ever occur since the algorithm is fixed.
            e.printStackTrace();
        }
    }

    /**
     * The implementation of the pybossa platform.
     * Call init after instantiation.
     * You need to setup a project on the specified pybossa platform before you can use this.
     *
     * @param workerServiceUrl  the url of the worker-service
     * @param workerUiPublicUrl the public url of the worker-ui
     * @param workerUiLocalUrl  the local url of the worker-ui
     * @param apiKey            the api key to access the pyboss api
     * @param baseUrl           the platform base url
     * @param name              the name of the platform
     * @param projectID         the project id
     * @param calibsAllowed     true if calibrations are allowed
     */
    public PyBossaPlatform(String workerServiceUrl, String workerUiPublicUrl, String workerUiLocalUrl, String apiKey, String baseUrl,
                           String name, String projectID, Boolean calibsAllowed) {
        this(workerServiceUrl, workerUiPublicUrl, apiKey, baseUrl, name, projectID, calibsAllowed);
        this.workerUiLocalUrl = workerUiLocalUrl;
    }

    /**
     * Initializes the pybossa idTasks and taskPresenter. This makes requests to the pybossa platform.
     *
     * @throws PyBossaRequestException if the requests to the platform fail (e.g. the platform cannot be reached).
     */
    public void init() {
        initializeTaskPresenter();
        initializeIdTasks();
    }

    @Override
    public Optional<Payment> getPayment() {
        return Optional.empty();
    }

    /**
     * if the Platform has his own worker identification the interface can be returned here.
     *
     * @return the value to indicate if it supports worker identification or not.
     */
    @Override
    public Optional<WorkerIdentificationComputation> getWorker() {
        return Optional.of(params -> WorkerIdentification.findByIdentification(getId(), identifyWorker(params)));
    }

    @Override
    public String getName() {
        return "PyBossa " + name;
    }

    @Override
    public String getRawId() {
        return ("pybossa " + name);
    }

    @Override
    public Boolean isCalibrationAllowed() {
        return this.calibsAllowed;
    }

    @Override
    public String getLink() {
        return baseUrl + "/project/" + projectShortName + "/newtask";
    }

    @Override
    public CompletableFuture<JsonElement> publishTask(Experiment experiment) {
        return CompletableFuture.supplyAsync(() -> {
            int task = requests.postTask(new JSONObject()
                    .put("project_id", projectID)
                    .put("info", new JSONObject()
                                    .put("type", "experiment")
                                    .put("url", workerServiceUrl)
                                    .put("platform", getId())
                                    .put("expID", experiment.getId())
                                    .put("idTasks", new JSONArray(idTasks))
                                    .put("paymentBase", experiment.getPaymentBase().getValue())
                                    .put("paymentRating", experiment.getPaymentRating().getValue())
                                    .put("paymentAnswer", experiment.getPaymentAnswer().getValue())
                            //pybossa doesn't support tags
                    )
                    // will have lowest priority, so idTasks will be visited first
                    .put("priority_0", 0)
                    .put("n_answers", experiment.getNeededAnswers().getValue()));
            JsonObject json = new JsonObject();
            json.add("identification", new JsonPrimitive(task));
            return json;
        });
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(JsonElement data) {
        return CompletableFuture.supplyAsync(() -> requests.deleteTask(data.getAsJsonObject().get("identification").getAsString()));
    }

    /**
     * IdentifyWorker takes the passed params and looks for a specified workerId, an idTask id and a code.
     * It then checks if there is one idTask that has been submitted by the worker before.
     * It hashes the passed code from the params and compares it to the previously hashed code saved in the taskRun.
     * If the hashes are equal, the workers identity is verified for this particular request.
     *
     * @param param the params passed from the worker ui
     * @return the worker's id
     * @throws UnidentifiedWorkerException if the worker cannot be identified
     */
    private String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        if (param == null) {
            throw new UnidentifiedWorkerException("The parameter map passed to PyBossaPlatform is null.");
        }
        String[] emptyDefault = {""};
        String givenWorkerId = param.getOrDefault("workerId", emptyDefault)[0];
        String givenIdTask = param.getOrDefault("idTask", emptyDefault)[0];
        String givenCode = param.getOrDefault("code", emptyDefault)[0];

        String errorMessage = "";
        if (!givenWorkerId.isEmpty() && !givenCode.isEmpty() && !givenIdTask.isEmpty()) {
            // check if valid idTask
            if (Ints.contains(idTasks, java.lang.Integer.parseInt(givenIdTask))) {
                JSONArray taskRuns = requests.getTaskRuns(givenIdTask, givenWorkerId);
                // if all given values are valid there should only be one task run returned
                if (taskRuns.length() == 1) {
                    // delete task run anyway, so the idTask can be used again
                    requests.deleteTaskRun(taskRuns.getJSONObject(0).getInt("id"));
                    String savedCode = taskRuns.getJSONObject(0).getJSONObject("info").optString("code", "");
                    // the saved code is the sha256 representation of the given code in base64url encoding
                    // this hashes the givenCode in sha256 and compares it to the decoded savedCode
                    if (MessageDigest.isEqual(Base64.getUrlDecoder().decode(savedCode),
                            messageDigest.digest(givenCode.getBytes()))) {
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
     * Initializes the task presenter by fetching the html from the given url
     * and setting the task presenter for the configured pybossa platform.
     */
    private void initializeTaskPresenter() {
        String html = requests.getStringFromUrl(workerUiLocalUrl + "/platform/pybossa.html");
        String workerUiLibraryUrl = workerUiLocalUrl + "/worker_ui.js";
        projectShortName = requests.getProject().getString("short_name");
        if (requests.existsUrl(workerUiLibraryUrl)) {
            // replace worker-ui url
            html = html.replaceFirst("(<script id=\"worker_ui\" src=\")(.+)(\"></script>)", "$1" + workerUiPublicUrl + "/worker_ui.js$3");
            // replace project short name
            html = html.replaceFirst("(var projectName = \")(.+)(\";)", "$1" + projectShortName + "$3");
            requests.setTaskPresenter(html);
        } else {
            throw new PyBossaRequestException(String.format("Could not find the worker ui library under the url \"%s\".",
                    workerUiLibraryUrl));
        }
    }

    /**
     * Checks for existing idTasks.
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
                // will have the highest priority
                .put("priority_0", 1)
                .put("info", new JSONObject()
                        .put("type", "idTask")
                        .put("url", workerServiceUrl)
                        .put("platform", getId())
                )
                //max int
                .put("n_answers", 2147483647);
        return requests.postTask(jsonTask);
    }
}
