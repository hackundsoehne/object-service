package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

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
    private MTurkRestApi api;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/integration-test/resources/mturk.properties"));
        api = new MTurkRestApi(
                properties.getProperty("User"),
                properties.getProperty("Password"),
                "https://mechanicalturk.sandbox.amazonaws.com/");
    }

    @Test
    public void testPublishHIT() throws Exception {
        String id = api.publishHIT("Description2","Title1",
                0.20,60,2000,"test,for,everything",
                2,2000000,"data",new Date().toString()).get();

        assertNotEquals(id, null);

        HIT hit = api.getHit(id).get();

        assertEquals(hit.getTitle(), "Title1");
        assertEquals(hit.getDescription(), "Description2");
        assertTrue(hit.getReward().getAmount().subtract(new BigDecimal(0.20)).doubleValue() < 0.0001);
        assertEquals(hit.getAssignmentDurationInSeconds(), new Long(60));
        //FIXME check the lifetime
        assertEquals(hit.getKeywords(), "test,for,everything");
        assertEquals(hit.getMaxAssignments(), new Integer(2));
        assertEquals(hit.getAutoApprovalDelayInSeconds(), new Long(2000000));
        assertEquals(hit.getRequesterAnnotation(), "data");

        assertTrue(api.unpublishHIT(id).get());

        assertEquals(api.getHit(id).get().getHITStatus(), HITStatus.DISPOSED);

    }
}