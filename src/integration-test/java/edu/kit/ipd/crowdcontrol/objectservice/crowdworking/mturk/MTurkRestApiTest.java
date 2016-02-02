package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.GetHIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.PublishHIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.UnpublishHIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.HIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.HITStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

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
        String id = new PublishHIT(connection, new Date().toString(),"Title1", "Description",
                0.20,60,2000,"test,for,everything",
                2,2000000,"data").get();

        assertNotEquals(id, null);

        HIT hit = new GetHIT(connection,id,"").get();

        assertEquals(hit.getTitle(), "Title1");
        assertEquals(hit.getDescription(), "Description2");
        assertTrue(hit.getReward().getAmount().subtract(new BigDecimal(0.20)).doubleValue() < 0.0001);
        assertEquals(hit.getAssignmentDurationInSeconds(), new Long(60));
        //FIXME check the lifetime
        assertEquals(hit.getKeywords(), "test,for,everything");
        assertEquals(hit.getMaxAssignments(), new Integer(2));
        assertEquals(hit.getAutoApprovalDelayInSeconds(), new Long(2000000));
        assertEquals(hit.getRequesterAnnotation(), "data");

        assertTrue(new UnpublishHIT(connection,id,"").get());

        assertEquals(new GetHIT(connection,id,"").get().getHITStatus(), HITStatus.DISPOSED);

    }
}