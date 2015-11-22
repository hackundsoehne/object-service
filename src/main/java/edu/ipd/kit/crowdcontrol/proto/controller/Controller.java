package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.json.JSONExperiment;
import edu.ipd.kit.crowdcontrol.proto.json.JSONRequestChecker;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import spark.Request;
import spark.Response;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains various helper-methods useful to the Controllers.
 * @author LeanderK
 * @version 1.0
 */
public class Controller {
    protected final DSLContext create;
    protected final Gson gson =  new GsonBuilder()
            .registerTypeAdapter(JSONExperiment.class, new JSONRequestChecker<>())
            .create();

    public Controller(DSLContext create) {
        this.create = create;
    }


    protected void assertRequest(Request request, Predicate<Request> predicate, String message) {
        if (!predicate.test(request)) {
            throw new BadRequestException(message);
        }
    }

    protected void assertJson(Request request) {
        String actualContentType = request.headers("Content-Type");
        assertRequest(request, request1 -> actualContentType != null && actualContentType.equals("application/json"),
                "Content-Type must be 'application/json'!");
    }

    protected String assertParameter(Request request, String parameter) {
        assertRequest(request, request1 -> request1.params(parameter) != null, "Request needs Parameter:" + parameter);
        return request.params(parameter);
    }

    protected int assertParameterInt(Request request, String parameter) {
        assertRequest(request, request1 -> request1.params(parameter) != null, "Request needs Parameter:" + parameter);
        try {
            return Integer.parseInt(request.params(parameter));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Request needs Parameter: " + parameter + " as an Integer");
        }
    }

    protected Response createJson(Request request, Response response, Supplier<String> createJSON) {
        assertJson(request);
        String json = createJSON.get();
        response.body(json);
        response.status(200);
        response.type("application/json");
        return response;
    }

    protected Response processJson(Request request, Response response, Consumer<String> consumer) {
        assertJson(request);
        String json = request.body();
        response.status(200);
        response.body("error");
        consumer.accept(json);
        return response;
    }

    protected Response processJsonWithTransaction(Request request, Response response, BiConsumer<String, Configuration> consumer) {
        return processJson(request, response, json -> consumer.accept(json, create.configuration()));
    }
}
