package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.BadRequestException;
import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;
import edu.ipd.kit.crowdcontrol.proto.controller.StatisticsController;
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
    private final StatisticsController statisticsController;

    public Router(ExperimentController experimentController, CrowdComputingController crowdComputingController, StatisticsController statisticsController) {
        this.experimentController = experimentController;
        this.crowdComputingController = crowdComputingController;
        this.statisticsController = statisticsController;
    }

    public void init() {
        exception(BadRequestException.class, (e, request, response) -> {
            response.status(400);
            response.body(e.getMessage());
            System.err.println("Bad Request! " + request.toString() + " error: " + e.getMessage());
        });

        get("/experiments/delete", experimentController::deleteExperiment);

        post("/experiments/create", experimentController::createExperiment);

        post("/experiments/update", experimentController::updateExperiment);

        get("/experiments", experimentController::getExperiment);

        get("/crowd/start", crowdComputingController::startExperiment);

        get("/crowd/running", crowdComputingController::getRunning);

        get("/crowd/stop", crowdComputingController::stopExperiment);

        get("/crowd/stop", crowdComputingController::stopExperiment);

        get("/statistics/hits", statisticsController::getAllHitsOverview);

        get("/statistics/csv", statisticsController::getCSV);

        get("/statistics/csv/answers", statisticsController::getAnswersCSV);

        get("/statistics/csv/ratings", statisticsController::getRatingsCSV);

        get("/statistics/tasks", statisticsController::getSimpleTasksJSON);

        get("/statistics/detail/hit", statisticsController::getHit);

        get("/statistics/detail/task", statisticsController::getFullTaskJSON);
    }
}
