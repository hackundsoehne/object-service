package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.ExperimentOperator;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.PopulationsHelper;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private ExperimentsPlatformOperations experimentsPlatformOperations;
    private PlatformManager platformManager;
    private ExperimentOperator experimentOperator;
    private AnswerRatingOperations answerRatingOperations;
    private ExperimentFetcher experimentFetcher;
    private PopulationsHelper populationsHelper;

    @Before
    public void setUp() {
        request = mock(Request.class);
        response = mock(Response.class);

        answerRatingOperations = mock(AnswerRatingOperations.class);
        experimentOperations = mock(ExperimentOperations.class);
        calibrationOperations = mock(CalibrationOperations.class);
        tagConstraintsOperations = mock(TagConstraintsOperations.class);
        algorithmOperations = mock(AlgorithmOperations.class);
        experimentsPlatformOperations = mock(ExperimentsPlatformOperations.class);
        experimentOperator = mock(ExperimentOperator.class);
        platformManager = mock(PlatformManager.class);
        experimentFetcher = mock(ExperimentFetcher.class);
        populationsHelper = mock(PopulationsHelper.class);

        resource = new ExperimentResource(answerRatingOperations, experimentOperations, calibrationOperations, tagConstraintsOperations, algorithmOperations, experimentsPlatformOperations,  platformManager, experimentOperator, experimentFetcher, populationsHelper);
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

        Experiment experiment = Experiment.newBuilder().addAllRatingOptions(generateDefaultRatingOptions()).setDescription("{{Person}}").build();
        when(request.attribute("input")).thenReturn(experiment);

        resource.put(request, response);
    }

    @Test
    public void succeedsWithoutInconsistencyBetweenDescriptionAndPlaceholdersInPutWithoutPlaceholders() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Everything went fine");

        Experiment experiment = Experiment.newBuilder().addAllRatingOptions(generateDefaultRatingOptions()).setDescription("foobar").build();
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

        Experiment experiment = Experiment.newBuilder().addAllRatingOptions(generateDefaultRatingOptions()).setDescription("{{foobar}}").putAllPlaceholders(placeholders).build();
        when(request.attribute("input")).thenReturn(experiment);

        when(experimentOperations.insertNewExperiment(any())).thenThrow(new RuntimeException("Everything went fine"));

        resource.put(request, response);
    }

    private List<Experiment.RatingOption> generateDefaultRatingOptions() {
        List<Experiment.RatingOption> ratingOptions = new ArrayList<>();

        ratingOptions.add(Experiment.RatingOption.newBuilder().setName("Good").setValue(9).build());
        ratingOptions.add(Experiment.RatingOption.newBuilder().setName("Bad").setValue(0).build());

        return ratingOptions;
    }
}
