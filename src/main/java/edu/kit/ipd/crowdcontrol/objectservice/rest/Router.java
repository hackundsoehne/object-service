package edu.kit.ipd.crowdcontrol.objectservice.rest;

import com.google.protobuf.Message;
import edu.kit.ipd.crowdcontrol.objectservice.proto.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.transformer.InputTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.rest.transformer.OutputTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.util.Arrays;
import java.util.stream.Collectors;

import static spark.Spark.before;
import static spark.Spark.exception;

/**
 * Application entry point. Defines the REST API.
 *
 * @author Niklas Keller
 */
public class Router implements SparkApplication {
    private static final Logger LOGGER = LogManager.getLogger(Router.class);

    private final TemplateResource templateResource;
    private final NotificationResource notificationResource;
    private final PlatformResource platformResource;
    private final WorkerResource workerResource;
    private final CalibrationResource calibrationResource;
    private final ExperimentResource experimentResource;
    private final AlgorithmResources algorithmResources;
    private final AnswerRatingResource answerRatingResource;
    private final WorkerCalibrationResource workerCalibrationResource;

    /**
     * Creates a new instance. Call {@link #init()} afterwards to initialize the routes.
     */
    public Router(TemplateResource templateResource,
                  NotificationResource notificationResource,
                  PlatformResource platformResource,
                  WorkerResource workerResource,
                  CalibrationResource calibrationResource,
                  ExperimentResource experimentResource,
                  AlgorithmResources algorithmResources,
                  AnswerRatingResource answerRatingResource,
                  WorkerCalibrationResource workerCalibrationResource) {
        this.templateResource = templateResource;
        this.notificationResource = notificationResource;
        this.platformResource = platformResource;
        this.calibrationResource = calibrationResource;
        this.workerResource = workerResource;
        this.experimentResource = experimentResource;
        this.algorithmResources = algorithmResources;
        this.answerRatingResource = answerRatingResource;
        this.workerCalibrationResource = workerCalibrationResource;
    }

    @Override
    public void init() {
        LOGGER.trace("Setting up routes for Spark.");

        exception(BadRequestException.class, (exception, request, response) -> {
            response.status(400);
            response.body(error(request, response, "badRequest", exception.getMessage()));
        });

        exception(NotFoundException.class, (exception, request, response) -> {
            response.status(404);
            response.body(error(request, response, "notFound", exception.getMessage()));
        });

        exception(NotAcceptableException.class, (exception, request, response) -> {
            // Don't use error(...) in this handler,
            // otherwise we end up throwing the same exception again.

            response.status(406);
            response.type("text/plain");
            response.body("notAcceptable: " + exception.getMessage());
        });

        exception(UnsupportedMediaTypeException.class, (exception, request, response) -> {
            String[] acceptedTypes = ((UnsupportedMediaTypeException) exception).getSupportedTypes();
            String accept = Arrays.stream(acceptedTypes).collect(Collectors.joining(","));

            response.status(415);
            response.header("accept", accept);
            response.body(error(request, response, "unsupportedMediaType", exception.getMessage()));
        });

        exception(InternalServerErrorException.class, (exception, request, response) -> {
            LOGGER.error(exception);

            response.status(500);
            response.body(error(request, response, "internalServerError", exception.getMessage()));
        });

        before((request, response) -> {
            if (request.headers("accept") == null) {
                throw new BadRequestException("Missing required 'accept' header.");
            }
        });

        put("/templates", templateResource::put, Template.class);
        get("/templates", templateResource::all);
        get("/templates/:id", templateResource::get);
        patch("/templates/:id", templateResource::patch, Template.class);
        delete("/templates/:id", templateResource::delete);

        put("/notifications", notificationResource::put, Notification.class);
        get("/notifications", notificationResource::all);
        get("/notifications/:id", notificationResource::get);
        patch("/notifications/:id", notificationResource::patch, Notification.class);
        delete("/notifications/:id", notificationResource::delete);

        get("/platforms", platformResource::all);
        get("/platforms/:id", platformResource::get);

        put("/calibrations", calibrationResource::put, Calibration.class);
        get("/calibrations", calibrationResource::all);
        get("/calibrations/:id", calibrationResource::get);
        delete("/calibrations/:id", calibrationResource::delete);

        get("/workers/:platform/identity", workerResource::identify);
        put("/workers", workerResource::put, Worker.class);
        get("/workers", workerResource::all);
        get("/workers/:id", workerResource::get);
        patch("/workers/:id", workerResource::patch, Worker.class);
        delete("/workers/:id", workerResource::delete);
        put("/workers/:id/calibrations", workerCalibrationResource::put, CalibrationAnswer.class);

        get("/algorithms", algorithmResources::getAllAlgortihms);

        put("/experiments", experimentResource::put, Experiment.class);
        get("/experiments", experimentResource::all);
        get("/experiments/:id", experimentResource::get);
        patch("/experiments/:id", experimentResource::patch, Experiment.class);
        delete("/experiments/:id", experimentResource::delete);
        put("/experiments/:id/answers", answerRatingResource::putAnswer, Answer.class);
        get("/experiments/:id/answers", answerRatingResource::getAnswers);
        get("/experiments/:id/answers/:aid", answerRatingResource::getAnswer);
        put("/experiments/:id/answers/:aid/rating", answerRatingResource::putRating, Rating.class);

        LOGGER.trace("Finished setting up routes for Spark.");
    }

    /**
     * Creates a new GET route.
     *
     * @param path
     *         Path.
     * @param route
     *         Handler.
     */
    private void get(String path, Route route) {
        Spark.get(path, new OutputTransformer(route));
    }

    /**
     * Creates a new PUT route.
     *
     * @param path
     *         Path.
     * @param route
     *         Handler.
     * @param type
     *         Protocol buffer type.
     */
    private void put(String path, Route route, Class<? extends Message> type) {
        Spark.put(path, new InputTransformer(new OutputTransformer(route), type));
    }

    /**
     * Creates a new PATCH route.
     *
     * @param path
     *         Path.
     * @param route
     *         Handler.
     * @param type
     *         Protocol buffer type.
     */
    private void patch(String path, Route route, Class<? extends Message> type) {
        Spark.patch(path, new InputTransformer(new OutputTransformer(route), type));
    }

    /**
     * Creates a new DELETE route.
     *
     * @param path
     *         Path.
     * @param route
     *         Handler.
     */
    private void delete(String path, Route route) {
        Spark.delete(path, new OutputTransformer(route));
    }

    /**
     * Creates an error response and encodes it into JSON / protocol buffers.
     *
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     * @param code
     *         Short error code to make errors machine readable.
     * @param detail
     *         Detailed error message for humans.
     *
     * @return Encoded message.
     */
    private String error(Request request, Response response, String code, String detail) {
        ErrorResponse error = ErrorResponse.newBuilder().setCode(code).setDetail(detail).build();
        return OutputTransformer.transform(request, response, error);
    }
}