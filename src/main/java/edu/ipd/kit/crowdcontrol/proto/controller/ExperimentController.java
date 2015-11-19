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

    public Response createExperiment(Request request) {

    }
}
