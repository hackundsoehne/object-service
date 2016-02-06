package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import com.google.protobuf.Message;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.Main;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RunWith (Suite.class)
@Suite.SuiteClasses({
        CalibrationResourceIntegrationTest.class,
        TemplateResourceIntegrationTest.class
})
public class ResourceIntegrationTest {
    protected static final String ORIGIN = "http://localhost:4567";

    @BeforeClass
    public static void setUp() throws IOException, ConfigException {
        Main.main(null);
    }

    @AfterClass
    public static void tearDown() {
        Spark.stop();
    }

    protected <T extends Message> T httpGet(String path, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.get(ORIGIN + path)
                .header("accept", "application/protobuf")
                .asBinary();

        return fromResponse(response, type);
    }

    protected <T extends Message> T httpPut(String path, T input, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.put(ORIGIN + path)
                .header("accept", "application/protobuf")
                .header("content-type", "application/protobuf")
                .body(input.toByteArray())
                .asBinary();

        return fromResponse(response, type);
    }

    protected <T extends Message> T httpPatch(String path, T input, Class<T> type) throws UnirestException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        HttpResponse<InputStream> response = Unirest.patch(ORIGIN + path)
                .header("accept", "application/protobuf")
                .header("content-type", "application/protobuf")
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
