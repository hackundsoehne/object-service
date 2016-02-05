package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformConnectionException;
import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatformTest {
    static PyBossaPlatform pybossa;
    private static final String WORKER_SERVICE_URL = "http://example.com";
    private static final String API_KEY = "8ec92fa1-1bd1-42ad-8524-3d2bab4588b1";
    private static final String API_URL = "http://localhost:5000/api";
    private static final String TASK_URL = API_URL + "/task";
    private static final String NAME = "pybossa";
    private static final int PROJECT_ID = 1;

    @BeforeClass
    public static void setUp() throws Exception {
        deleteAllTasks(TASK_URL, API_KEY, PROJECT_ID);
        pybossa = new PyBossaPlatform(WORKER_SERVICE_URL, API_KEY,
                API_URL, NAME, PROJECT_ID, true);
    }

    @Test
    public void testInitIdTasks() throws Exception {

    }

    @Test
    public void testGetWorker() throws Exception {

    }

    @Test
    public void testPublishTask() throws Exception {

    }

    @Test
    public void testUnpublishTask() throws Exception {

    }

    @Test
    public void testUpdateTask() throws Exception {

    }

    private static void deleteAllTasks(String taskUrl, String apiKey, int projectId) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(taskUrl)
                    .header("Content-Type", "application/json")
                    .queryString("api_key", apiKey)
                    .queryString("project_id", projectId)
                    .asJson();
        } catch (UnirestException e) {
            throw new PlatformConnectionException(e);
        }

        JSONArray tasks = response.getBody().getArray();

        for (int i = 0; i < tasks.length(); i++) {
            try {
                Unirest.delete(taskUrl + "/{id}")
                        .queryString("api_key", apiKey)
                        .routeParam("id", Integer.toString(tasks.getJSONObject(i).getInt("id")))
                        .asJson();
            } catch (UnirestException e) {
                throw new PlatformConnectionException(e);
            }
        }
    }
}