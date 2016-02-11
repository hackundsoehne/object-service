package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class wraps all requests to the pybossa platform API.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaRequests {
    private final String taskUrl;
    private final int projectId;
    private final String apiKey;
    private final String apiUrl;

    /**
     * The constructor of PyBossaRequests
     *
     * @param apiUrl    the url of the api
     * @param projectId the project id
     * @param apiKey    the api key
     */
    public PyBossaRequests(String apiUrl, int projectId, String apiKey) {
        this.apiUrl = apiUrl;
        this.taskUrl = apiUrl + "/task";
        this.projectId = projectId;
        this.apiKey = apiKey;
    }

    /**
     * Gets all tasks from the platform
     *
     * @return all tasks in the project. Might be empty.
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

    /**
     * Posts a new task to the platform. That task will be publicly visible afterwards
     *
     * @param task the task to post
     * @return the task id
     */
    public int postTask(JSONObject task) {
        JsonNode jsonNode = new JsonNode(task.toString());

        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(taskUrl)
                    .header("Content-Type", "application/json")
                    .queryString("api_key", apiKey)
                    .body(jsonNode)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }

        if (response.getStatus() == 200) {
            return response.getBody().getObject().getInt("id");
        } else {
            throw new PyBossaRequestException(response.getBody().getObject()
                    .optString("exception_msg", "Publishing task failed"));
        }
    }

    /**
     * Updates a given task.
     *
     * @param jsonTask the task to update
     * @return the updated task
     */
    public JSONObject updateTask(JSONObject jsonTask) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.put(taskUrl + "/{taskId}")
                    .header("Content-Type", "application/json")
                    .routeParam("taskId", String.valueOf(jsonTask.getInt("id")))
                    .queryString("api_key", apiKey)
                    .body(jsonTask)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(e);
        }
        if (response.getStatus() == 200) {
            return response.getBody().getObject();
        } else {
            throw new PyBossaRequestException(response.getBody().getObject()
                    .optString("exception_msg", "Updating task failed"));
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

    /**
     * Deletes a task from the platform.
     *
     * @param id the task to delete
     * @return true if successful, else false
     */
    public boolean deleteTask(int id) {
        return deleteTask(String.valueOf(id));
    }

    /**
     * Get all taskRuns for the given task and user.
     *
     * @param taskId the taskId
     * @param userId the userId
     * @return all taskRuns, might be empty
     */
    public JSONArray getTaskRuns(String taskId, String userId) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get(apiUrl + "/taskrun")
                    .queryString("api_key", apiKey)
                    .queryString("task_id", taskId)
                    .queryString("user_id", userId)
                    .asJson();
        } catch (UnirestException e) {
            throw new PyBossaRequestException(String.format("could not get taskRuns for task %s and worker %s",
                    taskId, userId), e);
        }

        JsonNode body = response.getBody();
        if (body.isArray()) {
            return body.getArray();
        }
        return new JSONArray();
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

    public void deleteTaskRun(int taskRunId) {
        deleteTaskRun(String.valueOf(taskRunId));
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
