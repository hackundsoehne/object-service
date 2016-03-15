package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Integer;
import org.ho.yaml.Yaml;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Simon Korz
 * @version 1.0
 */
@Ignore
public class PyBossaPlatformTest {
    private static String workerServiceUrl;
    private static String workerUiUrl;
    private static String apiKey;
    private static String apiUrl;
    private static String taskUrl;
    private static String name;
    private static int projectId = 1;

    private static Experiment experiment = Experiment.newBuilder()
            .setId(1)
            .setTitle("Test Experiment")
            .setDescription("Test description")
            .setPaymentBase(Integer.newBuilder().setValue(5).build())
            .setNeededAnswers(Integer.newBuilder().setValue(5).build())
            .setRatingsPerAnswer(Integer.newBuilder().setValue(5).build())
            .build();

    private static PyBossaRequests requests;
    private static PyBossaPlatform pybossa;

    @BeforeClass
    public static void setUp() throws Exception {
        // load config
        File configFile = new File("src/integration-test/resources/config.yml");
        Config config = Yaml.loadType(configFile, Config.class);
        workerServiceUrl = config.deployment.workerService;
        workerUiUrl = config.deployment.workerUIPublic;
        for (ConfigPlatform platform : config.platforms) {
            if (platform.type.toLowerCase().equals("pybossa")) {
                if (config.deployment.workerUILocal == null) {
                    config.deployment.workerUILocal = config.deployment.workerUIPublic;
                }
                apiKey = platform.apiKey;
                apiUrl = platform.url;
                name = platform.name;
                projectId = java.lang.Integer.valueOf(platform.projectId);
            }
        }
        taskUrl = apiUrl + "/task";
        requests = new PyBossaRequests(apiUrl, projectId, apiKey);

        deleteAllTasks(taskUrl, apiKey, projectId);
        pybossa = new PyBossaPlatform(workerServiceUrl, workerUiUrl, apiKey,
                apiUrl, name, String.valueOf(projectId), true);
        pybossa.init();
    }



    @Test
    public void testPublishTask() throws Exception {
        JsonElement publishedId = pybossa.publishTask(experiment).get();

        // get task with id = publishedid
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(taskUrl + "/{id}")
                    .queryString("api_key", apiKey)
                    .routeParam("id", publishedId.getAsJsonObject().get("identification").getAsString())
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }
        // experiment published
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testUnpublishTask() throws Exception {
        // get first available task
        JSONArray tasks = requests.getAllTasks();
        String taskId = String.valueOf(tasks.optJSONObject(0).getInt("id"));
        CompletableFuture<Boolean> booleanCompletableFuture = pybossa.unpublishTask(new JsonPrimitive(taskId));
        assertTrue(booleanCompletableFuture.get());
    }

    @Test
    public void testSetTaskPresenter() throws Exception {
        String html = "<html></html>";
        requests.setTaskPresenter(html);
        JSONObject project = requests.getProject();

        assertEquals(project.getJSONObject("info").getString("task_presenter"), html);
    }

    @Test
    public void testUpdateTask() throws Exception {
        requests.postTask(new JSONObject())
    }



    private static void deleteAllTasks(String TASK_URL, String apiKey, int projectId) {
        JSONArray tasks = requests.getAllTasks();

        for (int i = 0; i < tasks.length(); i++) {
            try {
                Unirest.delete(TASK_URL + "/{id}")
                        .queryString("api_key", apiKey)
                        .routeParam("id", String.valueOf(tasks.getJSONObject(i).getInt("id")))
                        .asJson();
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
        }
    }
}