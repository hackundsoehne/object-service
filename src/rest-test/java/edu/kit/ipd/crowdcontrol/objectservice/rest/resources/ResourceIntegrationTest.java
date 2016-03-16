package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import edu.kit.ipd.crowdcontrol.objectservice.Main;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import edu.kit.ipd.crowdcontrol.objectservice.proto.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Integer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Ignore
public class ResourceIntegrationTest {
    protected static final String ORIGIN = "http://localhost:4567";

    @BeforeClass
    public static void setUp() throws IOException, ConfigException, SQLException {
        Main.main(null);
    }

    @AfterClass
    public static void tearDown() {
        Spark.stop();
    }

    @Test
    public void calibrations() throws Exception {
        CalibrationList list = httpGet("/calibrations", CalibrationList.class);
        assertSame(0, list.getItemsCount());

        Calibration put = Calibration.newBuilder()
                .setName("Gender")
                .setQuestion("What's your gender?")
                .addAnswers(Calibration.Answer.newBuilder().setAnswer("male").build())
                .addAnswers(Calibration.Answer.newBuilder().setAnswer("female").build())
                .addAcceptedAnswers(Calibration.Answer.newBuilder().setAnswer("male").build())
                .build();

        Calibration calibration = httpPut("/calibrations", put, Calibration.class);

        // ID gets ignored
        assertTrue(calibration.getId() > 0);

        // Clear all answer IDs
        Calibration.Builder calibrationWithoutAnswerIds = calibration.toBuilder().clearId();
        List<Calibration.Answer> answers = calibrationWithoutAnswerIds.getAnswersList().stream().map(answer -> answer.toBuilder().clearId().build()).collect(Collectors.toList());

        calibrationWithoutAnswerIds.clearAnswers();
        calibrationWithoutAnswerIds.addAllAnswers(answers);

        // Accepted answers must not be saved
        assertEquals(put.toBuilder().clearAcceptedAnswers().build(), calibrationWithoutAnswerIds.build());

        // Returned calibration must be equal to the returned version when using GET
        Calibration received = httpGet("/calibrations/" + calibration.getId(), Calibration.class);
        assertEquals(calibration, received);

        list = httpGet("/calibrations", CalibrationList.class);
        assertSame(1, list.getItemsCount());

        assertNull(httpDelete("/calibrations/" + calibration.getId(), Calibration.class));

        list = httpGet("/calibrations", CalibrationList.class);
        assertSame(0, list.getItemsCount());

        ErrorResponse error = httpGet("/calibrations/42", ErrorResponse.class);
        assertEquals("notFound", error.getCode());
    }

    @Test
    public void templates() throws Exception {
        TemplateList list = httpGet("/templates", TemplateList.class);
        assertSame(0, list.getItemsCount());

        Template put = Template.newBuilder()
                .setName("name")
                .setContent("content")
                .setAnswerType(AnswerType.TEXT)
                .addConstraints(Constraint.newBuilder().setName("constraint").build())
                .addTags(Tag.newBuilder().setName("tag").build())
                .addRatingOptions(Template.RatingOption.newBuilder().setName("good").setValue(9).build())
                .addRatingOptions(Template.RatingOption.newBuilder().setName("bad").setValue(0).build())
                .build();

        Template template = httpPut("/templates", put, Template.class);

        // ID gets ignored
        assertTrue(template.getId() > 0);

        // All properties except ID must be equal
        assertEquals(put, template.toBuilder().clearId().build());

        // Returned template must be equal to the returned version when using GET
        Template received = httpGet("/templates/" + template.getId(), Template.class);
        assertEquals(template, received);

        list = httpGet("/templates", TemplateList.class);
        assertSame(1, list.getItemsCount());

        assertNull(httpDelete("/templates/" + template.getId(), Template.class));

        list = httpGet("/templates", TemplateList.class);
        assertSame(0, list.getItemsCount());

        ErrorResponse error = httpGet("/templates/42", ErrorResponse.class);
        assertEquals("notFound", error.getCode());
    }

    @Test
    public void experiments() throws Exception {
        ExperimentList list = httpGet("/experiments", ExperimentList.class);
        assertSame(0, list.getItemsCount());

        Experiment put = Experiment.newBuilder()
                .setTitle("Awesome")
                .setDescription("Awesome Task!")
                .setAnswerType(AnswerType.TEXT)
                .setWorkerQualityThreshold(Integer.newBuilder().setValue(2))
                .setRatingsPerWorker(Integer.newBuilder().setValue(3))
                .setAnswersPerWorker(Integer.newBuilder().setValue(4))
                .setRatingsPerAnswer(Integer.newBuilder().setValue(5))
                .setPaymentBase(Integer.newBuilder().setValue(6))
                .setPaymentAnswer(Integer.newBuilder().setValue(7))
                .setPaymentRating(Integer.newBuilder().setValue(8))
                .setPaymentQualityThreshold(Integer.newBuilder().setValue(9))
                .setAlgorithmQualityAnswer(AlgorithmOption.newBuilder().setName("AnswerQualityByRatings").addParameters(AlgorithmOption.AlgorithmParameter.newBuilder().setId(1).setValue("5")))
                .setAlgorithmQualityRating(AlgorithmOption.newBuilder().setName("RatingQualityByDistribution"))
                .setAlgorithmTaskChooser(AlgorithmOption.newBuilder().setName("anti_spoof").addParameters(AlgorithmOption.AlgorithmParameter.newBuilder().setId(1).setValue("30pc")))
                .addConstraints(Constraint.newBuilder().setName("constraint").build())
                .addTags(Tag.newBuilder().setName("tag").build())
                .addRatingOptions(Experiment.RatingOption.newBuilder().setName("good").setValue(9).build())
                .addRatingOptions(Experiment.RatingOption.newBuilder().setName("bad").setValue(0).build())
                .build();

        Experiment experiment = httpPut("/experiments", put, Experiment.class);

        // ID gets ignored
        assertTrue(experiment.getId() > 0);

        // All properties except ID must be equal
        assertEquals(put, experiment.toBuilder().clearId().build());

        // Returned template must be equal to the returned version when using GET
        Experiment received = httpGet("/experiments/" + experiment.getId(), Experiment.class);
        assertEquals(experiment, received);

        list = httpGet("/experiments", ExperimentList.class);
        assertSame(1, list.getItemsCount());

        assertNull(httpDelete("/experiments/" + experiment.getId(), Experiment.class));

        list = httpGet("/experiments", ExperimentList.class);
        assertSame(0, list.getItemsCount());

        ErrorResponse error = httpGet("/experiments/42", ErrorResponse.class);
        assertEquals("notFound", error.getCode());
    }

    @Test
    public void unsupportedMediaType() throws Exception {
        HttpResponse<String> response;

        response = Unirest.get(ORIGIN + "/templates")
                .header("accept", "text/plain")
                .asString();

        assertEquals(406, response.getStatus());

        response = Unirest.put(ORIGIN + "/templates")
                .header("accept", "application/json")
                .body("").asString();

        assertEquals(415, response.getStatus());

        response = Unirest.put(ORIGIN + "/templates")
                .header("content-type", "text/plain")
                .header("accept", "application/json")
                .body("").asString();

        assertEquals(415, response.getStatus());
    }

    public static <T extends Message> T httpGet(String path, Class<T> type) throws Exception {
        HttpResponse<InputStream> response = Unirest.get(ORIGIN + path)
                .header("accept", "application/protobuf")
                .asBinary();

        return fromResponse(response, type);
    }

    public static <T extends Message> T httpPut(String path, T input, Class<T> type) throws Exception {
        HttpResponse<InputStream> response = Unirest.put(ORIGIN + path)
                .header("accept", "application/protobuf")
                .header("content-type", "application/protobuf")
                .body(input.toByteArray())
                .asBinary();

        return fromResponse(response, type);
    }

    public static <T extends Message> T httpPatch(String path, T input, Class<T> type) throws Exception {
        HttpResponse<InputStream> response = Unirest.patch(ORIGIN + path)
                .header("accept", "application/protobuf")
                .header("content-type", "application/protobuf")
                .body(input.toByteArray())
                .asBinary();

        return fromResponse(response, type);
    }

    public static <T extends Message> T httpDelete(String path, Class<T> type) throws Exception {
        HttpResponse<InputStream> response = Unirest.delete(ORIGIN + path)
                .header("accept", "application/protobuf")
                .asBinary();

        return fromResponse(response, type);
    }

    private static <T extends Message> T fromResponse(HttpResponse<InputStream> response, Class<T> type) throws Exception {
        if (response.getStatus() == 204) {
            return null;
        }

        if (response.getStatus() >= 400 && !(type.equals(ErrorResponse.class))) {
            try {
                ErrorResponse.Builder builder = ErrorResponse.newBuilder();
                builder.mergeFrom(response.getBody());

                throw new RuntimeException("Error (" + response.getStatus() + "): " + builder.getCode() + ": " + builder.getDetail());
            } catch (InvalidProtocolBufferException e) {
                try (java.util.Scanner s = new java.util.Scanner(response.getBody())) {
                    String body = s.useDelimiter("\\A").hasNext() ? s.next() : "";

                    throw new RuntimeException("Error (" + response.getStatus() + "): " + body);
                }
            }
        }

        Method method = type.getMethod("newBuilder");
        T.Builder builder = (T.Builder) method.invoke(null);

        builder.mergeFrom(response.getBody());

        return (T) builder.build();
    }
}
