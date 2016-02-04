package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.amazonaws.mturk.requester.doc._2014_08_15.Assignment;
import com.amazonaws.mturk.requester.doc._2014_08_15.HIT;
import com.amazonaws.mturk.requester.doc._2014_08_15.HITStatus;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
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

    @Test
    public void testPublishHIT() throws Exception {
        String id = new PublishHIT(connection,"Title1", "Description2",
                0.20,60,2000,"test,for,everything",
                2,2000000,"data").get();

        assertNotEquals(id, null);

        HIT hit = new GetHIT(connection,id).get();

        try {
            new RejectAssignment(connection,"alpha","bla").get();
            new ApproveAssignment(connection,"alpha","bla").get();
            new GetBonusPayments(connection,id,"bla2",1).get();
        } catch (ExecutionException e) {
            assertEquals(e.getCause().getCause().getMessage().replaceAll("\\d",""),
                   "AWS.MechanicalTurk.AssignmentDoesNotExist : " +
                            "Assignment ALPHA does not exist. ( s)\nAssignmentId = ALPHA");
        }

        List<Assignment> assignments = new GetAssignments(connection, id, 1).join();

        assertEquals(assignments.size(), 0);

        assertEquals(hit.getTitle(), "Title1");
        assertEquals(hit.getDescription(), "Description2");
        assertTrue(hit.getReward().getAmount().subtract(new BigDecimal(0.20)).doubleValue() < 0.0001);
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