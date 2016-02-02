package edu.kit.ipd.crowdcontrol.objectservice.event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by marcel on 31.01.16.
 */
public class EventLogTest {
    private EventLog eventLog;

    @Before
    public void setUp() throws Exception {
        eventLog = new EventLog();
    }

    @Test
    public void testtoString() throws Exception {
        eventLog.setName("bla");
        eventLog.addEntry("test1", "test2\n");

        String result = eventLog.toString();

        assertEquals(result, "bla : test1\ntest2\n\n");
    }
}