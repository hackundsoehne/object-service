package edu.ipd.kit.crowdcontrol.proto.controller;

import spark.Request;

import java.util.function.Predicate;

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
}
