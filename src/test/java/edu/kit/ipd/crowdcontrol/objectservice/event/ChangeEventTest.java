package edu.kit.ipd.crowdcontrol.objectservice.event;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by marcel on 16.03.16.
 */
public class ChangeEventTest {
    private ChangeEvent e;
    @Before
    public void setUp() throws Exception {
         e = new ChangeEvent<>(20, 21);
    }

    @Test
    public void testGetOld() throws Exception {
        assertEquals(e.getOld(), 20);
    }

    @Test
    public void testGetNeww() throws Exception {
        assertEquals(e.getNeww(), 21);
    }
}