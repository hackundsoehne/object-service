package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.CalibrationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.Range;
import edu.kit.ipd.crowdcontrol.objectservice.proto.CalibrationList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import org.junit.Before;
import org.junit.Test;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalibrationResourceTest {
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        request = mock(Request.class);
        response = mock(Response.class);
    }

    @Test
    public void test() {
        CalibrationOperations calibrationOperations = mock(CalibrationOperations.class);
        when(calibrationOperations.getCalibrationsFrom(0, true, 20)).thenReturn(Range.of(new ArrayList<>(), null, null, false, false));

        CalibrationResource resource = new CalibrationResource(calibrationOperations);
        Paginated<Integer> paginated = resource.all(request, response);

        assertEquals(paginated.getMessage(), CalibrationList.newBuilder().build());
        assertEquals(paginated.getLeft(), Optional.empty());
        assertEquals(paginated.getRight(), Optional.empty());
        assertSame(paginated.hasNext(), false);
        assertSame(paginated.hasPrevious(), false);
    }
}
