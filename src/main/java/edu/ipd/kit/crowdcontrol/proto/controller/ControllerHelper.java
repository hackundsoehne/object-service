package edu.ipd.kit.crowdcontrol.proto.controller;

import spark.Request;
import spark.Response;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains various helper-methods useful to the Controllers.
 * @author LeanderK
 * @version 1.0
 */
public interface ControllerHelper {
    default void assertRequest(Request request, Predicate<Request> predicate, String message) {
        if (!predicate.test(request)) {
            throw new BadRequestException(message);
        }
    }

    default void assertJson(Request request) {
        String actualContentType = request.headers("Content-Type");
        assertRequest(request, request1 -> actualContentType != null && actualContentType.equals("application/json"),
                "Content-Type must be 'application/json'!");
    }

    default String assertParameter(Request request, String parameter) {
        assertRequest(request, request1 -> request1.params(parameter) != null, "Request needs Parameter:" + parameter);
        return request.params(parameter);
    }

    default int assertParameterInt(Request request, String parameter) {
        assertRequest(request, request1 -> request1.params(parameter) != null, "Request needs Parameter:" + parameter);
        try {
            return Integer.parseInt(request.params(parameter));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Request needs Parameter: " + parameter + " as an Integer");
        }
    }

    default  Response createJson(Request request, Response response, Supplier<String> createJSON) {
        assertJson(request);
        String json = createJSON.get();
        response.body(json);
        response.status(200);
        response.type("application/json");
        return response;
    }
}
