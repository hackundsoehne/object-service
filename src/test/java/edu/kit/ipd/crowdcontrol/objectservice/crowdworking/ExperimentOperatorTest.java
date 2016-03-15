package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by marcel on 15.03.16.
 */
public class ExperimentOperatorTest {
    private ExperimentOperator experimentOperator;
    private PlatformManager platformManager;
    private Experiment experiment;

    @Before
    public void setUp() throws Exception {
        platformManager = mock(PlatformManager.class);
        experimentOperator = new ExperimentOperator(platformManager);
        experiment  = Experiment.newBuilder()
                        .addPopulations(Experiment.Population.newBuilder().setPlatformId("Good"))
                        .addPopulations(Experiment.Population.newBuilder().setPlatformId("Bad"))
                        .build();
    }
    @Test
    public void testStartExperiment() throws Exception {
        when(platformManager.publishTask("Good", experiment)).thenReturn(CompletableFuture.completedFuture(true));
        when(platformManager.publishTask("Bad", experiment)).thenReturn(CompletableFuture.completedFuture(false));
        when(platformManager.unpublishTask("Good", experiment)).thenReturn(CompletableFuture.completedFuture(true));

        experimentOperator.startExperiment(experiment);

        verify(platformManager).unpublishTask("Good", experiment);
    }

    @Test
    public void testEndExperiment() throws Exception {
        //FIXME enable this test once the sleep is gone
        /*when(platformManager.unpublishTask("Good",experiment)).thenReturn(CompletableFuture.completedFuture(true));
        when(platformManager.unpublishTask("Bad",experiment)).thenReturn(CompletableFuture.completedFuture(true));

        experimentOperator.endExperiment(experiment);

        verify(platformManager).unpublishTask("Good", experiment);
        verify(platformManager).unpublishTask("Bad", experiment);*/
    }
}