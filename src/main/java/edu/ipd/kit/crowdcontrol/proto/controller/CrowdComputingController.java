package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import org.jooq.DSLContext;
import spark.Request;
import spark.Response;

/**
 * manages all the requests which interact with a Crowd-Computing Platform.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class CrowdComputingController implements ControllerHelper {
    private final DSLContext create;
    private final Experiment experiment = Tables.EXPERIMENT;
    private final GsonBuilder gsonBuilder =  new GsonBuilder();

    public CrowdComputingController(DSLContext create) {
        this.create = create;
    }

    public Response startExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        response.status(200);
        response.type("text/plain");

        return response;
    }

    public Response getRunning(Request request, Response response) {
        response.status(200);
        response.type("application/json");
        return response;
    }

    public Response stopExperiment(Request request, Response response) {
        response.status(200);
        response.type("text/plain");
        return response;
    }
}
