package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.proto.ErrorResponse;
import org.junit.Before;
import org.junit.Test;
import spark.Request;
import spark.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OutputTransformerTest {
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        this.request = mock(Request.class);
        this.response = mock(Response.class);
    }

    @Test
    public void transformJson() {
        when(request.headers("accept")).thenReturn("application/json");

        ErrorResponse error = ErrorResponse.newBuilder().setCode("OK").build();
        String output = OutputTransformer.transform(request, response, error);

        verify(response).type("application/json");
        assertEquals("{\n  \"code\": \"OK\"\n}", output);
    }

    @Test
    public void transformProto() {
        when(request.headers("accept")).thenReturn("application/protobuf");

        ErrorResponse error = ErrorResponse.newBuilder().setCode("OK").build();
        String output = OutputTransformer.transform(request, response, error);

        verify(response).type("application/protobuf");
        assertEquals(new String(error.toByteArray()), output);
    }

    @Test (expected = NotAcceptableException.class)
    public void transformError() {
        when(request.headers("accept")).thenReturn("text/html");

        ErrorResponse error = ErrorResponse.newBuilder().setCode("OK").build();
        OutputTransformer.transform(request, response, error);
    }

    @Test
    public void transformDefaultJson() {
        when(request.headers("accept")).thenReturn("*/*");

        ErrorResponse error = ErrorResponse.newBuilder().setCode("OK").build();
        String output = OutputTransformer.transform(request, response, error);

        verify(response).type("application/json");
        assertEquals("{\n  \"code\": \"OK\"\n}", output);
    }
}
