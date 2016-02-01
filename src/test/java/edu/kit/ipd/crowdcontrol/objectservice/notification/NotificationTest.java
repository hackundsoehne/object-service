package edu.kit.ipd.crowdcontrol.objectservice.notification;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class NotificationTest {
    private Notification notification;
    private static final int THRESHOLD = 60 * 60 * 24;

    @Before
    public void setUp() throws Exception {
        notification = new Notification(1, "Name", "Desc", THRESHOLD, 60 * 60, "query", null);
    }

    @Test
    public void testThresholdPassed() throws Exception {
        Instant yesterday = Instant.now().minusSeconds(THRESHOLD + 1);
        notification.setLastSent(yesterday);

        //lastSent is one day and one second ago sendThreshold is one day
        assertTrue("Notification threshold not passed", notification.thresholdPassed());
    }
}