package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa;

import com.google.gson.JsonElement;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentificationComputation;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Integer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class PyBossaPlatformUnitTest {
    private static final String WORKER_SERVICE_URL = "http://localhost:8080";
    private static final String WORKER_UI_URL = "http://localhost:3000";
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

    private static MessageDigest messageDigest;

    private static String idTask = "2";
    private static final String WORKER_ID = "37";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private static PyBossaRequests requests;
    @InjectMocks
    private static PyBossaPlatform pybossa = new PyBossaPlatform(WORKER_SERVICE_URL, WORKER_UI_URL, API_KEY, API_URL, NAME,
            String.valueOf(PROJECT_ID), true);

    @Before
    public void setUp() throws Exception {
        messageDigest = MessageDigest.getInstance("SHA-256");

        MockitoAnnotations.initMocks(this);
        when(requests.getAllTasks()).thenReturn(getAllTasksDummy(3, 2));
        when(requests.getStringFromUrl(anyString())).thenReturn("");
        when(requests.existsUrl(anyString())).thenReturn(true);
        when(requests.getProject()).thenReturn(new JSONObject().put("short_name", "test"));
        pybossa.init();
    }

    @Test
    public void testGetWorker() throws Exception {
        Optional<WorkerIdentificationComputation> workerIdentification = pybossa.getWorker();

        String code = "super code";
        String hashedEncodedCode = Base64.getUrlEncoder().encodeToString(messageDigest.digest(code.getBytes()));
        // create a tusk run with the hashed code
        JSONArray taskRun = new JSONArray().put(new JSONObject()
                .put("id", 2)
                .put("user_id", WORKER_ID)
                .put("info", new JSONObject()
                        .put("code", hashedEncodedCode)));
        when(requests.getTaskRuns(idTask, WORKER_ID)).thenReturn(taskRun);
        // simulate params that would be passed by the worker-ui
        HashMap<String, String[]> params = new HashMap<>();
        params.put("idTask", new String[]{idTask});
        params.put("workerId", new String[]{WORKER_ID});
        params.put("code", new String[]{code});

        //try to get worker
        JsonElement verifiedWorkerId = workerIdentification.get().getWorker(params).getWorkerData();

        assertEquals(WORKER_ID, verifiedWorkerId.getAsString());
    }

    private static JSONArray getAllTasksDummy(int idTaskCount, int experimentTaskCount) {
        JSONArray tasks = new JSONArray();
        for (int i = 1; i <= idTaskCount; i++) {
            tasks.put(new JSONObject()
                    .put("id", i)
                    .put("info", new JSONObject()
                            .put("type", "idTask"))
            );
        }
        for (int i = idTaskCount; i <= experimentTaskCount + idTaskCount; i++) {
            tasks.put(new JSONObject()
                    .put("id", i)
                    .put("info", new JSONObject()
                            .put("type", "experiment"))
            );
        }
        return tasks;
    }
}