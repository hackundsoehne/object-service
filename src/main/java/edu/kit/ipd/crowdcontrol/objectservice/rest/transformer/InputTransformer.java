package edu.kit.ipd.crowdcontrol.objectservice.rest.transformer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.UnsupportedMediaTypeException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Method;

/**
 * Transforms request bodies into protocol buffers.
 *
 * @author Niklas Keller
 */
public class InputTransformer implements Route {
    private static final JsonFormat.Parser PARSER = JsonFormat.parser();

    private final Route next;
    private final Class<? extends Message> type;

    /**
     * @param next Route that consumes the transformed input.
     * @param type Expected protocol buffer type.
     */
    public InputTransformer(Route next, Class<? extends Message> type) {
        this.next = next;
        this.type = type;
    }

    /**
     * Transforms the body of the request into a protocol buffer object and saves it as {@code
     * input} attribute in the request.
     *
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     * @return Returns the result of the {@code next} route.
     */
    public Object handle(Request request, Response response) throws Exception {
        String body = request.body();
        String contentType = request.contentType();

        Method method = this.type.getMethod("newBuilder");
        Message.Builder builder = (Message.Builder) method.invoke(null);

        try {
            switch (contentType) {
                case "application/json":
                    PARSER.merge(body, builder);
                    break;
                case "application/protobuf":
                    // https://tools.ietf.org/html/draft-rfernando-protocol-buffers-00
                    builder.mergeFrom(body.getBytes());
                    break;
                default:
                    throw new UnsupportedMediaTypeException(contentType, "application/json", "application/protobuf");
            }
        } catch (InvalidProtocolBufferException e) {
            throw new BadRequestException("Invalid protocol buffer.");
        }

        request.attribute("input", builder.build());
        return next.handle(request, response);
    }
}
