package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;
import spark.servlet.SparkApplication;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 *
 * @author LeanderK
 * @version 1.0
 */
public class Router implements SparkApplication {
    private final ExperimentController experimentController;

    public Router(ExperimentController experimentController) {
        this.experimentController = experimentController;
    }

    public void init() {
        get("/experiments/delete/:expID", (request, response) -> {
            //delete exp
            return null;
        });

        post("/experiments/create/:expID", experimentController::createExperiment);

        get("/experiments/:expID", (request, response) -> {
            //get exp
            return null;
        });

    }
}
