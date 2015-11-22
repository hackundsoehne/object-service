package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.BadRequestException;
import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;
import edu.ipd.kit.crowdcontrol.proto.controller.ResourceNotFoundExcpetion;
import spark.servlet.SparkApplication;

import static spark.Spark.*;

/**
 * The Router routes and is responsible for the routing.
 *
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class Router implements SparkApplication {
    private final ExperimentController experimentController;
    private final CrowdComputingController crowdComputingController;

    public Router(ExperimentController experimentController, CrowdComputingController crowdComputingController) {
        this.experimentController = experimentController;
        this.crowdComputingController = crowdComputingController;
    }

    public void init() {
        exception(BadRequestException.class, (e, request, response) -> {
            response.status(400);
            response.body(e.getMessage());
            System.err.println("Bad Request! " + request.toString() + " error: " + e.getMessage());
        });

        exception(ResourceNotFoundExcpetion.class, (e, request, response) -> {
            response.status(404);
            response.body(e.getMessage());
            System.err.println("Resource not found! " + request.toString() + " error: " + e.getMessage());
        });

        //experiemtn/<id>/start
        get("/experiments/delete", experimentController::deleteExperiment);

        post("/experiments/create", experimentController::createExperiment);

        post("/experiments/update", experimentController::updateExperiment);

        get("/experiments", experimentController::getExperiment);

        get("/crowd/start", crowdComputingController::startHIT);

        get("/crowd/running", crowdComputingController::getRunning);

        get("/crowd/stop", crowdComputingController::stopHIT);

        put("/crowd/update", crowdComputingController::updateHIT);

        //TODO: shutdown and calling Unirest.shutdown();
    }
}
