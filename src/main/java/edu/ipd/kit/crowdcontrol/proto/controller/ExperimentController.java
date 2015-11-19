package edu.ipd.kit.crowdcontrol.proto.controller;

import org.jooq.DSLContext;
import spark.Request;
import spark.Response;

/**
 * Holds all the queries.
 * @author LeanderK
 * @version 1.0
 */
public class ExperimentController {
    private final DSLContext create;

    public ExperimentController(DSLContext create) {
        this.create = create;
    }

    public Response createExperiment(Request request, Response response) {
        String actualContentType = request.headers("Content-Type");
        if (actualContentType == null || !actualContentType.equals("application/json")) {
            response.body("Content-Type must be 'application/json'!");
            response.status(400);
            response.type("text/plain");
            return response;
        }

        String id = request.params("expID");

    }
}
