package edu.kit.ipd.crowdcontrol.objectservice.rest.transformer;

import edu.kit.ipd.crowdcontrol.objectservice.proto.ErrorResponse;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.UnsupportedMediaTypeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import spark.Request;
import spark.Response;
import spark.Route;

import static org.mockito.Mockito.*;

public class InputTransformerTest {
    private Request request;
    private Response response;
    private Route route;

    /**
     * @link http://stackoverflow.com/a/6527990/2373138
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        this.request = mock(Request.class);
        this.response = mock(Response.class);
        this.route = mock(Route.class);
    }

    @Test
    public void transformJson() throws Exception {
        InputTransformer transformer = new InputTransformer(route, ErrorResponse.class);

        when(request.contentType()).thenReturn("application/json");
        when(request.body()).thenReturn("{}");

        transformer.handle(request, response);

        verify(request).attribute("input", ErrorResponse.newBuilder().build());
    }

    @Test
    public void transformProto() throws Exception {
        InputTransformer transformer = new InputTransformer(route, ErrorResponse.class);

        when(request.contentType()).thenReturn("application/protobuf");
        when(request.bodyAsBytes()).thenReturn(ErrorResponse.newBuilder().build().toByteArray());

        transformer.handle(request, response);

        verify(request).attribute("input", ErrorResponse.newBuilder().build());
    }

    @Test
    public void transformInvalidProto() throws Exception {
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("Invalid protocol buffer: Protocol message contained an invalid tag (zero).");

        InputTransformer transformer = new InputTransformer(route, ErrorResponse.class);

        when(request.contentType()).thenReturn("application/protobuf");
        when(request.bodyAsBytes()).thenReturn(new byte[] {0, 0});

        transformer.handle(request, response);
    }

    @Test (expected = UnsupportedMediaTypeException.class)
    public void transformUnknown() throws Exception {
        InputTransformer transformer = new InputTransformer(route, ErrorResponse.class);

        when(request.contentType()).thenReturn("application/foobar");
        when(request.body()).thenReturn(new String(ErrorResponse.newBuilder().build().toByteArray()));

        transformer.handle(request, response);
    }
}
