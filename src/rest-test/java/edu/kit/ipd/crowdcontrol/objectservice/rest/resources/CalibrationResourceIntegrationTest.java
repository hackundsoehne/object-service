package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import com.mashape.unirest.http.exceptions.UnirestException;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import edu.kit.ipd.crowdcontrol.objectservice.proto.CalibrationList;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.resources.ResourceIntegrationTest.httpDelete;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.resources.ResourceIntegrationTest.httpGet;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.resources.ResourceIntegrationTest.httpPut;
import static org.junit.Assert.*;

public class CalibrationResourceIntegrationTest {
    @Test
    public void test() throws NoSuchMethodException, UnirestException, IllegalAccessException, IOException, InvocationTargetException {
        CalibrationList list = httpGet("/calibrations", CalibrationList.class);
        assertSame(0, list.getItemsCount());

        Calibration put = Calibration.newBuilder()
                .setName("Gender")
                .setQuestion("What's your gender?")
                .addAnswers(Calibration.Answer.newBuilder().setAnswer("answer1").build())
                .addAnswers(Calibration.Answer.newBuilder().setAnswer("answer2").build())
                .addAcceptedAnswers(Calibration.Answer.newBuilder().setAnswer("answer1").build())
                .build();

        Calibration calibration = httpPut("/calibrations", put, Calibration.class);

        assertTrue(calibration.getId() > 0);
        assertEquals(calibration.toBuilder().setId(0).clearAcceptedAnswers().clearAnswers().build(), put.toBuilder().clearAnswers().build());

        Calibration received = httpGet("/calibrations/" + calibration.getId(), Calibration.class);
        assertEquals(calibration, received);

        list = httpGet("/calibrations", CalibrationList.class);
        assertSame(1, list.getItemsCount());

        assertNull(httpDelete("/calibrations/" + calibration.getId(), Calibration.class));

        list = httpGet("/calibrations", CalibrationList.class);
        assertSame(0, list.getItemsCount());
    }
}
