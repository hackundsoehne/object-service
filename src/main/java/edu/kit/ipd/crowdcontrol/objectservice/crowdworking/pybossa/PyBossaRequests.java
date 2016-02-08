package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaRequests {
    private final String taskUrl;
    private final int projectId;
    private final String apiKey;

    public PyBossaRequests(String taskUrl, int projectId, String apiKey) {
        this.taskUrl = taskUrl;
        this.projectId = projectId;
        this.apiKey = apiKey;
    }

    /**
     * Gets all tasks from the platform
     *
     * @return all tasks in the project.
     */
    public JSONArray getAllTasks() {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(taskUrl)
                    .header("Content-Type", "application/json")
                    .queryString("api_key", apiKey)
                    .queryString("project_id", projectId)
                    .asJson();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        JsonNode body = response.getBody();
        if (body.isArray()) {
            return body.getArray();
        }
        return new JSONArray();
    }

    /**
     * Deletes a task from the platform
     *
     * @param id the task to delete
     * @return true if successful, else false
     */
    public boolean deleteTask(String id) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.delete(taskUrl + "/{taskId}")
                    .header("Content-Type", "application/json")
                    .routeParam("taskId", id)
                    .queryString("api_key", apiKey)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }
        // if response status is 204 the task was successfully deleted
        return response.getStatus() == 204;
    }
}
