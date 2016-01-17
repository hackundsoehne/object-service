package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.MimeParse;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms protocol buffer objects into JSON / protocol buffer responses.
 *
 * @author Niklas Keller
 */
public class OutputTransformer implements Route {
    // We wrap the original route here, because Spark's ResponseTransformer doesn't allow access to
    // Request and Response objects. We need access to the accept header and must change the type of
    // the response.

    private static final String TYPE_JSON = "application/json";
    private static final String TYPE_PROTOBUF = "application/protobuf";

    private static final List<String> SUPPORTED_TYPES;

    static {
        SUPPORTED_TYPES = new ArrayList<>();
        SUPPORTED_TYPES.add("application/protobuf");
        SUPPORTED_TYPES.add("application/json"); // Last to be default.
    }

    private Route route;

    /**
     * @param route
     *         Actual route to handle the request.
     */
    public OutputTransformer(Route route) {
        this.route = route;
    }

    @Override
    public String handle(Request request, Response response) throws Exception {
        Object result = this.route.handle(request, response);

        if (result == null) {
            // We can't use void handlers, so null is fine here. Ensure correct HTTP status code.
            response.status(204);

            // We must return an empty string explicitly,
            // otherwise it's encoded to "null" literally.
            return "";
        }

        if (!(result instanceof Message)) {
            // Unfortunately, we can't statically check this,
            // because of the method signature of Spark's Route interface.
            throw new InternalServerErrorException("Route did not respond with a protocol buffer.");
        }

        return transform(request, response, (Message) result);
    }

    /**
     * Transforms a protocol buffer object into an JSON / protocol buffer response based on the
     * accept header of the request.
     *
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     * @param message
     *         Protocol buffer to transform.
     */
    public static String transform(Request request, Response response, Message message) {
        String bestMatch = MimeParse.bestMatch(SUPPORTED_TYPES, request.headers("accept"));

        try {
            switch (bestMatch) {
                case TYPE_JSON:
                    response.type(TYPE_JSON);
                    return JsonFormat.printer().print(message);
                case TYPE_PROTOBUF:
                    response.type(TYPE_PROTOBUF);
                    return new String(message.toByteArray());
                default:
                    throw new NotAcceptableException(request.headers("accept"), TYPE_JSON, TYPE_PROTOBUF);
            }
        } catch (InvalidProtocolBufferException e) {
            throw new InternalServerErrorException("Attempt to transform an invalid protocol buffer into JSON.");
        }
    }
}