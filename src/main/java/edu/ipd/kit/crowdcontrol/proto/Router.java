package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.*;
import edu.ipd.kit.crowdcontrol.proto.web.FreeMarkerEngine;
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
    private final TaskController taskController;
    private final FreeMarkerEngine freeMarkerEngine;
    private final StatisticsController statisticsController;

    public Router(ExperimentController experimentController, CrowdComputingController crowdComputingController, TaskController taskController, FreeMarkerEngine freeMarkerEngine, StatisticsController statisticsController) {
        this.experimentController = experimentController;
        this.crowdComputingController = crowdComputingController;
        this.taskController = taskController;
        this.freeMarkerEngine = freeMarkerEngine;
        this.statisticsController = statisticsController;
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

        exception(InternalServerErrorException.class, (e, request, response) -> {
            response.status(500);
            response.body(e.getMessage());
            System.err.println("Internal server error for: " + request.toString() + " error: " + e.getMessage());
        });

        // Serve static files from src/main/resources/public
        staticFileLocation("/public");

        //experiemtn/<id>/start
        get("/experiments/delete", experimentController::deleteExperiment);

        post("/experiments/create", experimentController::createExperiment);

        post("/experiments/update", experimentController::updateExperiment);

        get("/experiments", experimentController::getExperiment);

        get("/crowd/start", crowdComputingController::startHIT);

        get("/crowd/running", crowdComputingController::getRunning);

        get("/crowd/stop", crowdComputingController::stopHIT);

        get("/crowd/update", crowdComputingController::updateHIT);

        get("/statistics/hits", statisticsController::getAllHitsOverview);

        get("/statistics/csv", statisticsController::getCSV);

        get("/statistics/csv/answers", statisticsController::getAnswersCSV);

        get("/statistics/csv/ratings", statisticsController::getRatingsCSV);

        get("/statistics/tasks", statisticsController::getSimpleTasksJSON);

        get("/statistics/detail/hit", statisticsController::getHit);

        get("/statistics/detail/task", statisticsController::getFullTaskJSON);

        get("/tasks/answer/render", taskController::renderAnswerTask, freeMarkerEngine);

        get("/tasks/rating/render", taskController::renderRatingTask, freeMarkerEngine);

        post("/tasks/rating/submit", taskController::submitRatingTask);

        post("/tasks/answer/submit", taskController::submitAnswerTask);

        //TODO: shutdown and calling Unirest.shutdown();
    }
}
