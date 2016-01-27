package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.json.JSONObject;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatform {
    private String apiKey;
    private String apiUrl;
    private String name;
    private String projectID;

    public PyBossaPlatform(String apiKey) {
        this.apiKey = apiKey;

        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initProject() {
        HttpResponse<JsonNode> response = Unirest.get(apiUrl + "task")
                .queryString("api_key", apiKey)
                .queryString("project_id", projectID)
                .asJson();

        JsonNode json = response.getBody();

        if json.isArray() && json.getArray().length {

        }
    }

    private void initIdTasks() {
        JsonNode jsonTask = new JsonNode();
        jsonTask.getObject().append("project_id", projectID)
                .append("info", new JSONObject()
                        .append("idTask"))
                .append("priority_0", 0);

        HttpResponse<JsonNode> postResponse = Unirest.post(apiUrl + "task")
                .header("Content-Type", "application/json")
                .queryString("api_key", apiKey)
                .body(jsonTask)
                .asJson();
    }

    private publishTask(Experiment experiment) {
        JsonNode jsonTask = new JsonNode();
        jsonTask.getObject().append("project_id", projectID)
                .append("info", new JSONObject()
                        .append("url", workerServiceUrl)
                        .append("expID", experiment.getId())
                        .append("platformName", name)
                        .append("idTask1", idTask1)
                        .append("idTask2", idTask2)
                )
                .append("priority_0", 1);

        HttpResponse<JsonNode> postResponse = Unirest.post(apiUrl + "task")
                .header("Content-Type", "application/json")
                .queryString("api_key", apiKey)
                .body(jsonTask)
                .asJson();

    }
}
