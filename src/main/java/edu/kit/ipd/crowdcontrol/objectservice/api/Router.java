package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.Message;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
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
    private Gson gson;
    private TemplateResource templateResource;

    /**
     * Creates a new instance. Call {@link #init()} afterwards to initialize the routes.
     */
    public Router() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.templateResource = new TemplateResource();
    }

    @Override
    public void init() {
        exception(BadRequestException.class, (exception, request, response) -> {
            response.status(400);
            response.type("application/json");
            response.body(gson.toJson(new ErrorResponse("badRequest", exception.getMessage())));
        });

        exception(NotAcceptableException.class, (exception, request, response) -> {
            response.status(406);
            response.type("application/json");
            response.body(gson.toJson(new ErrorResponse("notAcceptable", exception.getMessage())));
        });

        exception(UnsupportedMediaTypeException.class, (exception, request, response) -> {
            String[] acceptedTypes = ((UnsupportedMediaTypeException) exception).getSupportedTypes();
            String accept = Arrays.stream(acceptedTypes).collect(Collectors.joining(","));

            response.status(415);
            response.header("accept", accept);
            response.body(gson.toJson(new ErrorResponse("unsupportedMediaType", exception.getMessage())));
        });

        exception(InternalServerErrorException.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            response.body(gson.toJson(new ErrorResponse("internalServerError", exception.getMessage())));
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
}