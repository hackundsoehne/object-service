package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Integer;
import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Simon Korz
 * @version 1.0
 */
@Ignore
public class PyBossaPlatformTest {
    private static final String WORKER_SERVICE_URL = "http://localhost:8080";
    private static final String API_KEY = "8ec92fa1-1bd1-42ad-8524-3d2bab4588b1";
    private static final String API_URL = "http://localhost:5000/api";
    private static final String TASK_URL = API_URL + "/task";
    private static final String NAME = "pybossa";
    private static final int PROJECT_ID = 1;

    private static Experiment experiment = Experiment.newBuilder()
            .setId(1)
            .setTitle("Test Experiment")
            .setDescription("Test description")
            .setPaymentBase(Integer.newBuilder().setValue(5).build())
            .setNeededAnswers(Integer.newBuilder().setValue(5).build())
            .setRatingsPerAnswer(Integer.newBuilder().setValue(5).build())
            .build();

    private static PyBossaRequests requests = new PyBossaRequests(API_URL, PROJECT_ID, API_KEY);
    private static PyBossaPlatform pybossa;

    @BeforeClass
    public static void setUp() throws Exception {
        deleteAllTasks(TASK_URL, API_KEY, PROJECT_ID);
        pybossa = new PyBossaPlatform(WORKER_SERVICE_URL, API_KEY,
                API_URL, NAME, String.valueOf(PROJECT_ID), true);
        pybossa.init();
    }



    @Test
    public void testPublishTask() throws Exception {
        String publishedId = pybossa.publishTask(experiment).get();

        // get task with id = publishedid
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(TASK_URL + "/{id}")
                    .queryString("api_key", API_KEY)
                    .routeParam("id", publishedId)
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
        CompletableFuture<Boolean> booleanCompletableFuture = pybossa.unpublishTask(taskId);
        assertTrue(booleanCompletableFuture.get());
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