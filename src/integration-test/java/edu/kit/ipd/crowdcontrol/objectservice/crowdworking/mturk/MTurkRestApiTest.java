package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.amazonaws.mturk.requester.doc._2014_08_15.Assignment;
import com.amazonaws.mturk.requester.doc._2014_08_15.HIT;
import com.amazonaws.mturk.requester.doc._2014_08_15.HITStatus;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import edu.kit.ipd.crowdcontrol.objectservice.Main;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Created by marcel on 01.02.16.
 */
public class MTurkRestApiTest {
    private MTurkConnection connection;
    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/integration-test/resources/mturk.properties"));
        connection = new MTurkConnection(
                properties.getProperty("User"),
                properties.getProperty("Password"),
                "https://mechanicalturk.sandbox.amazonaws.com/");
    }

    @Test(expected = ExecutionException.class)
    public void testReject() throws Exception {
        new RejectAssignment(connection,"alpha","bla").get();
    }

    @Test(expected = ExecutionException.class)
    public void testApprove() throws Exception {
        new ApproveAssignment(connection,"alpha","bla").get();
    }

    @Test(expected = ExecutionException.class)
    public void testGetBonusPayments() throws Exception {
        new GetBonusPayments(connection,"bla",1).get();
    }

    @Test
    public void testPublishHIT() throws Exception {
        String id = new PublishHIT(connection,"Title1", "Description2",
                0.20,60,2000,"test,for,everything",
                2,2000000,"data","","").get();

        assertNotEquals(id, null);

        HIT hit = new GetHIT(connection,id).get();

        List<Assignment> assignments = new GetAssignments(connection, id, 1).join();

        assertEquals(assignments.size(), 0);

        assertEquals(hit.getTitle(), "Title1");
        assertEquals(hit.getDescription(), "Description2");
        assertEquals(new BigDecimal(0.20).doubleValue(), hit.getReward().getAmount().doubleValue(), 0.0001);
        assertEquals(hit.getAssignmentDurationInSeconds(), new Long(60));
        //FIXME check the lifetime
        assertEquals(hit.getKeywords(), "test,for,everything");
        assertEquals(hit.getMaxAssignments(), new Integer(2));
        assertEquals(hit.getAutoApprovalDelayInSeconds(), new Long(2000000));
        assertEquals(hit.getRequesterAnnotation(), "data");

        assertTrue(new UnpublishHIT(connection,id).get());

        assertEquals(new GetHIT(connection,id).get().getHITStatus(), HITStatus.DISPOSED);


    }
}