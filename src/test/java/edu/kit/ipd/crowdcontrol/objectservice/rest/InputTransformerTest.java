package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.proto.ErrorResponse;
import org.junit.Before;
import org.junit.Test;
import spark.Request;
import spark.Response;
import spark.Route;

import static org.mockito.Mockito.*;

public class InputTransformerTest {
    private Request request;
    private Response response;
    private Route route;

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
        when(request.body()).thenReturn(new String(ErrorResponse.newBuilder().build().toByteArray()));

        transformer.handle(request, response);

        verify(request).attribute("input", ErrorResponse.newBuilder().build());
    }

    @Test (expected = UnsupportedMediaTypeException.class)
    public void transformUnknown() throws Exception {
        InputTransformer transformer = new InputTransformer(route, ErrorResponse.class);

        when(request.contentType()).thenReturn("application/foobar");
        when(request.body()).thenReturn(new String(ErrorResponse.newBuilder().build().toByteArray()));

        transformer.handle(request, response);
    }
}
