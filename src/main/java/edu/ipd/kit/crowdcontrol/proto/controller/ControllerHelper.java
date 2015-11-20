package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jooq.TableRecord;
import spark.Request;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface ControllerHelper {
    default void assertRequest(Request request, Predicate<Request> predicate, String message) {
        if (!predicate.test(request)) {
            throw new BadRequestException(message);
        }
    }

    default void assertJson(Request request) {
        String actualContentType = request.headers("Content-Type");
        assertRequest(request, request1 -> actualContentType != null && actualContentType.equals("application/json"),
                "Content-Type must be 'application/json'!");
    }

    default String assertParameter(Request request, String parameter) {
        assertRequest(request, request1 -> request1.params(parameter) != null, "Request needs Parameter:" + parameter);
        return request.params(parameter);
    }

    default <T extends TableRecord<T>> T createRecord(T t, BiFunction<T, Map.Entry<String, JsonElement>, T> modifier, JsonObject jsonObject) {
        return jsonObject.entrySet().stream()
                .map(entry -> (Function<T, T>) step -> modifier.apply(step, entry))
                .reduce(Function.identity(), (f1, f2) -> f1.andThen(f2))
                .apply(t);
    }
}
