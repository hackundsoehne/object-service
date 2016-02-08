package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaRequests {
    private final String taskUrl;
    private final int projectId;
    private final String apiKey;
    private final String apiUrl;

    public PyBossaRequests(String apiUrl, int projectId, String apiKey) {
        this.apiUrl = apiUrl;
        this.taskUrl = apiUrl + "/task";
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
            throw new PyBossaRequestException(e);
        }
        JsonNode body = response.getBody();
        if (body.isArray()) {
            return body.getArray();
        }
        return new JSONArray();
    }

    public String postTask(JSONObject task) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(taskUrl)
                    .header("Content-Type", "application/json")
                    .queryString("api_key", apiKey)
                    .body(task)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }

        if (response.getStatus() == 200) {
            return String.valueOf(response.getBody().getObject().getInt("id"));
        } else {
            throw new PyBossaRequestException(response.getBody().getObject()
                    .optString("exception_msg", "Publishing task failed"));
        }
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

    private void deleteAllTaskRunsForTask(String task) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(apiUrl + "/taskrun")
                    .queryString("api_key", apiKey)
                    .queryString("task_id", task)

                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
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

    public void deleteTaskRun(String taskRunId) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.delete(apiUrl + "/taskrun/{id}")
                    .queryString("api_key", apiKey)
                    .routeParam("id", taskRunId)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }
        if (response.getStatus() != 204) {
            throw new PyBossaRequestException(String.format("Taskrun with id %s could not be deleted", taskRunId));
        }
    }
}
