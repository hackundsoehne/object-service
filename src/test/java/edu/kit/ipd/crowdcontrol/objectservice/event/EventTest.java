package edu.kit.ipd.crowdcontrol.objectservice.event;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void testEvent() throws Exception {
        Integer obj = 10;
        Event<Integer> event = new Event<>(obj);

        assertNotNull(event.getEventLog());
        assertEquals(event.getData(), obj);
    }
}