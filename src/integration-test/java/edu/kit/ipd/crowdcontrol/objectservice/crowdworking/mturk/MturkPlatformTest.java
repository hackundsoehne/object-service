package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PaymentJob;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.GrantBonus;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.NotifyWorker;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Integer;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by marcel on 04.02.16.
 */
public class MturkPlatformTest {
    private MturkPlatform platform;
    private Experiment experiment = Experiment.newBuilder()
            .setId(42)
            .setTitle("test")
            .setDescription("a")
            .setPaymentBase(Integer.newBuilder().setValue(5).build())
            .setNeededAnswers(Integer.newBuilder().setValue(5).build())
            .setRatingsPerAnswer(Integer.newBuilder().setValue(5).build())
            .build();
    private MTurkConnection connection;
    private String workerId;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/integration-test/resources/mturk.properties"));

        platform = new MturkPlatform(
                properties.getProperty("User"),
                properties.getProperty("Password"),
                "https://mechanicalturk.sandbox.amazonaws.com/",
                "Sandbox","http://example.com/", "http://worker.example.com/");
        platform.startExtenderService(Collections.emptyList());

        connection = new MTurkConnection(
                properties.getProperty("User"),
                properties.getProperty("Password"),
                "https://mechanicalturk.sandbox.amazonaws.com/");

        workerId = properties.getProperty("workerId");
    }

    @Test
    public void testNotify() throws Exception {
        new NotifyWorker(connection, workerId, "---", "testnotify").join();
    }

    @Test(expected = RuntimeException.class)
    public void testGrandBonus() throws Exception {
        new GrantBonus(connection, "---", workerId, 0.1, "Test Bonus").join();
    }

    @Test
    public void testPlatform() throws Exception {
        JsonElement data = platform.publishTask(experiment).join();

        platform.unpublishTask(data).join();

        //shound not fail
        platform.payExperiment(0, data, experiment, Collections.emptyList()).join();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailure() throws Exception {
        JsonElement id = platform.publishTask(experiment).join();

        platform.unpublishTask(id).join();

        //shound not fail
        List<PaymentJob> paymentJobs = new ArrayList<>();
        paymentJobs.add(new PaymentJob(new WorkerRecord(0,new JsonPrimitive("testworker"),"MTurk","",0, null),20, ""));
        platform.payExperiment(0, id, experiment, paymentJobs).join();
    }
}