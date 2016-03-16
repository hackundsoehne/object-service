package edu.kit.ipd.crowdcontrol.objectservice.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observer;
import rx.functions.Action;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by marcel on 16.03.16.
 */
public class EventObservableTest {
    private EventObservable<Integer> objserv;

    @Before
    public void setUp() throws Exception {
        objserv = new EventObservable<>();
    }

    @Test
    public void testEmit() throws Exception {
        Observer<? super Event<Integer>> action = mock(Observer.class);
        objserv.getObservable().subscribe(action);

        objserv.emit(10);

        verify(action).onNext(new Event<>(10));
    }

    @Test
    public void testGetObservable() throws Exception {
        assertNotNull(objserv.getObservable());
    }
}