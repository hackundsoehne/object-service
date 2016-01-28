package edu.kit.ipd.crowdcontrol.objectservice.rest.transformer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotAcceptableException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.MimeParse;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Transforms protocol buffer objects into JSON / protocol buffer responses.
 *
 * @author Niklas Keller
 */
public class OutputTransformer implements Route {
    // We wrap the original route here, because Spark's ResponseTransformer doesn't allow access to
    // Request and Response objects. We need access to the accept header and must change the type of
    // the response.

    private static final JsonFormat.Printer PRINTER = JsonFormat.printer();
    private static final String TYPE_JSON = "application/json";
    private static final String TYPE_PROTOBUF = "application/protobuf";
    private static final List<String> SUPPORTED_TYPES;

    static {
        List<String> supported_types = new ArrayList<>();
        supported_types.add("application/protobuf");
        supported_types.add("application/json"); // Last to be default.
        SUPPORTED_TYPES = Collections.unmodifiableList(supported_types);
    }

    private final Route route;

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

        Message message;

        if (result instanceof Message) {
            message = (Message) result;
        } else if (result instanceof Paginated) {
            Paginated<?> paginated = (Paginated) result;
            StringBuilder linkBuilder = new StringBuilder();

            if (paginated.hasPrevious()) {
                Map<String, String> params = new HashMap<>();
                paginated.getLeft().ifPresent(x -> params.put("from", x.toString()));
                params.put("asc", "false");

                linkBuilder.append("<").append(link(request, params)).append(">; rel=\"prev\", ");
            }

            if (paginated.hasNext()) {
                Map<String, String> params = new HashMap<>();
                paginated.getRight().ifPresent(x -> params.put("from", x.toString()));
                params.put("asc", "true");

                linkBuilder.append("<").append(link(request, params)).append(">; rel=\"next\", ");
            }

            String link = linkBuilder.toString();

            if (!link.isEmpty()) {
                link = link.substring(0, link.length() - 2); // remove trailing comma
                response.header("Link", link);
            }

            message = paginated.getMessage();
        } else {
            // Unfortunately, we can't statically check this,
            // because of the method signature of Spark's Route interface.
            throw new InternalServerErrorException("Route did not respond with a protocol buffer or paginated.");
        }

        return transform(request, response, message);
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
                    return PRINTER.print(message);
                case TYPE_PROTOBUF:
                    response.type(TYPE_PROTOBUF);
                    return new String(message.toByteArray());
                default:
                    throw new NotAcceptableException(request.headers("accept"), TYPE_JSON, TYPE_PROTOBUF);
            }
        } catch (InvalidProtocolBufferException e) {
            // Can't happen, because we don't use any "Any" fields.
            // https://developers.google.com/protocol-buffers/docs/proto3#any
            throw new InternalServerErrorException("Attempt to transform an invalid protocol buffer into JSON.");
        }
    }

    public static String link(Request request, Map<String, String> replaceQueryParams) {
        StringBuilder builder = new StringBuilder();
        URL url;

        try {
            url = new URL(request.url());
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("Request URI could not be parsed!");
        }

        builder.append(url.getPath()).append("?");

        try {
            for (String key : request.queryParams()) {
                if (replaceQueryParams.containsKey(key)) {
                    continue;
                }

                builder.append(URLEncoder.encode(key, "utf-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(request.queryParams(key), "utf-8"));
                builder.append("&");
            }

            for (String key : replaceQueryParams.keySet()) {
                builder.append(URLEncoder.encode(key, "utf-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(replaceQueryParams.get(key), "utf-8"));
                builder.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException("utf-8 is an unsupported encoding");
        }

        String link = builder.toString();

        if (link.isEmpty()) {
            return link;
        }

        return link.substring(0, link.length() - 1);
    }
}