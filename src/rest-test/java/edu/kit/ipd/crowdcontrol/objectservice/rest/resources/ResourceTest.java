package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import com.google.protobuf.Message;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract public class ResourceTest {
    protected static final String ORIGIN = "http://localhost:4567";

    protected <T extends Message> T httpGet(String path, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.get(ORIGIN + path)
                .header("accept", "application/protobuf")
                .asBinary();

        return fromResponse(response, type);
    }

    protected <T extends Message> T httpPut(String path, T input, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.put(ORIGIN + path)
                .header("accept", "application/protobuf")
                .body(input.toByteArray())
                .asBinary();

        return fromResponse(response, type);
    }

    protected <T extends Message> T httpPatch(String path, T input, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.patch(ORIGIN + path)
                .header("accept", "application/protobuf")
                .body(input.toByteArray())
                .asBinary();

        return fromResponse(response, type);
    }

    protected <T extends Message> T httpDelete(String path, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.delete(ORIGIN + path)
                .header("accept", "application/protobuf")
                .asBinary();

        return fromResponse(response, type);
    }

    private <T extends Message> T fromResponse(HttpResponse<InputStream> response, Class<T> type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (response.getStatus() == 204) {
            return null;
        }

        Method method = type.getMethod("newBuilder");
        T.Builder builder = (T.Builder) method.invoke(null);

        builder.mergeFrom(response.getBody());

        return (T) builder.build();
    }
}
