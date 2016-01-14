package edu.kit.ipd.crowdcontrol.objectservice.notification;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class NotificationTest {
    Notification notification;

    @Before
    public void setUp() throws Exception {
        notification = new Notification(1, "Name", "Desc", 60 * 60 * 24, 60 * 60 * 24, "query", null);
    }

    @Test
    public void testThresholdPassed() throws Exception {
        Instant yesterday = Instant.now().minus(Duration.ofDays(1).minusSeconds(1));
        notification.setLastSent(yesterday);

        //lastSent is one day and one second ago sendThreshold is one day
        assertTrue(notification.thresholdPassed());
    }
}