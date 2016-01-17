package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.protobuf.Message;
import edu.kit.ipd.crowdcontrol.objectservice.proto.ErrorResponse;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
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
    private TemplateResource templateResource;

    /**
     * Creates a new instance. Call {@link #init()} afterwards to initialize the routes.
     */
    public Router() {
        this.templateResource = new TemplateResource();
    }

    @Override
    public void init() {
        exception(BadRequestException.class, (exception, request, response) -> {
            response.status(400);
            response.body(error(request, response, "badRequest", exception.getMessage()));
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