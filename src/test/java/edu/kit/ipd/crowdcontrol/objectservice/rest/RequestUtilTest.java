package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import spark.Request;
import spark.Response;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestUtilTest {
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        request = mock(Request.class);
        response = mock(Response.class);
    }

    /**
     * @link http://stackoverflow.com/a/6527990/2373138
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void rejectGetParamIntIfParamNotFound() {
        expectedException.expect(InternalServerErrorException.class);
        expectedException.expectMessage("'id' not present.");

        RequestUtil.getParamInt(request, "id");
    }

    @Test
    public void rejectGetParamIntIfParamNotInteger() {
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("must be a valid integer");

        when(request.params("id")).thenReturn("abc");
        RequestUtil.getParamInt(request, "id");
    }

    @Test
    public void rejectGetQueryIntIfParamNotInteger() {
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("must be a valid integer");

        when(request.queryParams("id")).thenReturn("abc");
        RequestUtil.getQueryInt(request, "id", 0);
    }

    @Test
    public void usesDefaultValueIfGetQueryIntNotPresent() {
        when(request.queryParams("id")).thenReturn(null);
        assertSame(42, RequestUtil.getQueryInt(request, "id", 42));
    }

    @Test
    public void usesProvidedValueIfGetQueryIntPresent() {
        when(request.queryParams("id")).thenReturn("12");
        assertSame(12, RequestUtil.getQueryInt(request, "id", 42));
    }

    @Test
    public void rejectGetQueryBoolIfParamNotBoolean() {
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("must be a valid boolean");

        when(request.queryParams("id")).thenReturn("abc");
        RequestUtil.getQueryBool(request, "id", false);
    }

    @Test
    public void usesDefaultValueIfGetQueryBoolNotPresent() {
        when(request.queryParams("id")).thenReturn(null);
        assertSame(false, RequestUtil.getQueryBool(request, "id", false));
        assertSame(true, RequestUtil.getQueryBool(request, "id", true));
    }

    @Test
    public void usesProvidedValueIfGetQueryBoolPresentAndTrue() {
        when(request.queryParams("id")).thenReturn("true");
        assertSame(true, RequestUtil.getQueryBool(request, "id", false));
    }

    @Test
    public void usesProvidedValueIfGetQueryBoolPresentAndFalse() {
        when(request.queryParams("id")).thenReturn("false");
        assertSame(false, RequestUtil.getQueryBool(request, "id", false));
    }

    @Test
    public void usesProvidedValueIfGetQueryBoolPresentAnd1() {
        when(request.queryParams("id")).thenReturn("1");
        assertSame(true, RequestUtil.getQueryBool(request, "id", false));
    }

    @Test
    public void usesProvidedValueIfGetQueryBoolPresentAnd0() {
        when(request.queryParams("id")).thenReturn("0");
        assertSame(false, RequestUtil.getQueryBool(request, "id", false));
    }
}
