package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.DuplicateChecker;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
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
    private ExperimentFetcher experimentFetcher;
    private ExperimentsPlatformOperations experimentsPlatformOperations;
    private DuplicateChecker duplicateChecker;

    @Before
    public void setUp() throws Exception {
        this.duplicateChecker = mock(DuplicateChecker.class);
        experimentFetcher = mock(ExperimentFetcher.class);
        experimentsPlatformOperations = mock(ExperimentsPlatformOperations.class);
        platformManager = mock(PlatformManager.class);
        experimentOperator = new ExperimentOperator(platformManager,experimentFetcher,experimentsPlatformOperations,new EventManager(),duplicateChecker,1);
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
        when(platformManager.unpublishTask("Good",experiment)).thenReturn(CompletableFuture.completedFuture(true));
        when(platformManager.unpublishTask("Bad",experiment)).thenReturn(CompletableFuture.completedFuture(true));
        Set<ExperimentsPlatformStatusPlatformStatus> statuses = new HashSet<>();
        statuses.add(ExperimentsPlatformStatusPlatformStatus.running);
        when(experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(experiment.getId())).thenReturn(statuses);
        experimentOperator.endExperiment(experiment);

        verify(platformManager).unpublishTask("Good", experiment);
        verify(platformManager).unpublishTask("Bad", experiment);
    }
}