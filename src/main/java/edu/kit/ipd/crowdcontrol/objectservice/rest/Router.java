package edu.kit.ipd.crowdcontrol.objectservice.rest;

import com.google.protobuf.Message;
import edu.kit.ipd.crowdcontrol.objectservice.proto.ErrorResponse;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.NotificationResource;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.PlatformResource;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.TemplateResource;
import edu.kit.ipd.crowdcontrol.objectservice.rest.transformer.InputTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.rest.transformer.OutputTransformer;
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
    private final TemplateResource templateResource;
    private final NotificationResource notificationResource;
    private final PlatformResource platformResource;

    /**
     * Creates a new instance. Call {@link #init()} afterwards to initialize the routes.
     */
    public Router(TemplateResource templateResource, NotificationResource notificationResource, PlatformResource platformResource) {
        this.templateResource = templateResource;
        this.notificationResource = notificationResource;
        this.platformResource = platformResource;
    }

    @Override
    public void init() {
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