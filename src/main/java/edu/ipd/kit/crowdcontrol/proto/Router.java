package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.BadRequestException;
import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;
import edu.ipd.kit.crowdcontrol.proto.controller.TaskController;
import edu.ipd.kit.crowdcontrol.proto.view.FreeMarkerEngine;
import spark.servlet.SparkApplication;

import static spark.Spark.*;

/**
 * The Router routes and is responsible for the routing.
 *
 * @author LeanderK
 * @version 1.0
 */
public class Router implements SparkApplication {
    private final ExperimentController experimentController;
    private final CrowdComputingController crowdComputingController;
    private final TaskController taskController;

    public Router(ExperimentController experimentController, CrowdComputingController crowdComputingController, TaskController taskController) {
        this.experimentController = experimentController;
        this.crowdComputingController = crowdComputingController;
        this.taskController = taskController;
    }

    public void init() {
        exception(BadRequestException.class, (e, request, response) -> {
            response.status(400);
            response.body(e.getMessage());
            System.err.println("Bad Request! " + request.toString() + " error: " + e.getMessage());
        });

        //experiemtn/<id>/start
        get("/experiments/delete", experimentController::deleteExperiment);

        post("/experiments/create", experimentController::createExperiment);

        post("/experiments/update", experimentController::updateExperiment);

        get("/experiments", experimentController::getExperiment);

        get("/crowd/start", crowdComputingController::startExperiment);

        get("/crowd/running", crowdComputingController::getRunning);

        get("/crowd/stop", crowdComputingController::stopExperiment);

        get("/tasks/render", taskController::renderTask, new FreeMarkerEngine());
    }
}
