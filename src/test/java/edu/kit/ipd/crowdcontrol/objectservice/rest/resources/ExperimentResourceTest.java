package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Niklas Keller
 */
public class ExperimentResourceTest {
    private Request request;
    private Response response;
    private ExperimentResource resource;

    private ExperimentOperations experimentOperations;
    private CalibrationOperations calibrationOperations;
    private TagConstraintsOperations tagConstraintsOperations;
    private AlgorithmOperations algorithmOperations;
    private TasksOperations tasksOperations;
    private PlatformManager platformManager;

    @Before
    public void setUp() {
        request = mock(Request.class);
        response = mock(Response.class);

        experimentOperations = mock(ExperimentOperations.class);
        calibrationOperations = mock(CalibrationOperations.class);
        tagConstraintsOperations = mock(TagConstraintsOperations.class);
        algorithmOperations = mock(AlgorithmOperations.class);
        tasksOperations = mock(TasksOperations.class);
        platformManager = mock(PlatformManager.class);

        resource = new ExperimentResource(experimentOperations, calibrationOperations, tagConstraintsOperations, algorithmOperations, tasksOperations,  platformManager);
    }

    /**
     * @link http://stackoverflow.com/a/6527990/2373138
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void rejectsInconsistencyBetweenDescriptionAndPlaceholdersInPut() {
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("Description and placeholder keys must match.");

        Experiment experiment = Experiment.newBuilder().setDescription("{{Person}}").build();
        when(request.attribute("input")).thenReturn(experiment);

        resource.put(request, response);
    }

    @Test
    public void succeedsWithoutInconsistencyBetweenDescriptionAndPlaceholdersInPutWithoutPlaceholders() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Everything went fine");

        Experiment experiment = Experiment.newBuilder().setDescription("foobar").build();
        when(request.attribute("input")).thenReturn(experiment);

        when(experimentOperations.insertNewExperiment(any())).thenThrow(new RuntimeException("Everything went fine"));

        resource.put(request, response);
    }

    @Test
    public void succeedsWithoutInconsistencyBetweenDescriptionAndPlaceholdersInPutWithPlaceholders() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Everything went fine");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("foobar", "foo");

        Experiment experiment = Experiment.newBuilder().setDescription("{{foobar}}").putAllPlaceholders(placeholders).build();
        when(request.attribute("input")).thenReturn(experiment);

        when(experimentOperations.insertNewExperiment(any())).thenThrow(new RuntimeException("Everything went fine"));

        resource.put(request, response);
    }
}
