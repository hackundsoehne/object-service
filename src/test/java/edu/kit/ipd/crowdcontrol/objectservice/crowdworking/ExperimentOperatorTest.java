package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.DuplicateChecker;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
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
    private ExperimentsPlatformRecord platform1;
    private ExperimentsPlatformRecord platform2;

    @Before
    public void setUp() throws Exception {

        experiment  = Experiment.newBuilder().setId(1).build();

        platform1 = new ExperimentsPlatformRecord(0,experiment.getId(),"Good",new JsonPrimitive(""),"Good");
        platform2 = new ExperimentsPlatformRecord(1,experiment.getId(),"Bad",new JsonPrimitive(""),"Bad");


        experiment = experiment.toBuilder().addPopulations(Experiment.Population.newBuilder().setPlatformId(platform1.getIdentification()).build())
                .addPopulations(Experiment.Population.newBuilder().setPlatformId(platform2.getIdentification()).build())
                .build();

        this.duplicateChecker = mock(DuplicateChecker.class);
        experimentFetcher = mock(ExperimentFetcher.class);
        experimentsPlatformOperations = mock(ExperimentsPlatformOperations.class);
        platformManager = mock(PlatformManager.class);
        experimentOperator = new ExperimentOperator(platformManager,experimentFetcher,experimentsPlatformOperations,new EventManager(),duplicateChecker,1);



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
        when(platformManager.unpublishTask(platform1.getIdentification(),experiment)).thenReturn(CompletableFuture.completedFuture(true));
        when(platformManager.unpublishTask(platform2.getIdentification(),experiment)).thenReturn(CompletableFuture.completedFuture(true));
        Map<Integer, ExperimentsPlatformStatusPlatformStatus> statuses = new HashMap<>();
        statuses.put(platform1.getIdexperimentsPlatforms(), ExperimentsPlatformStatusPlatformStatus.running);
        statuses.put(platform2.getIdexperimentsPlatforms(),ExperimentsPlatformStatusPlatformStatus.creative_stopping);
        when(experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(experiment.getId())).thenReturn(statuses);
        when(experimentsPlatformOperations.getExperimentsPlatform(platform1.getIdexperimentsPlatforms())).thenReturn(Optional.of(platform1));
        when(experimentsPlatformOperations.getExperimentsPlatform(platform2.getIdexperimentsPlatforms())).thenReturn(Optional.of(platform2));
        experimentOperator.endExperiment(experiment);

        verify(platformManager).unpublishTask("Good", experiment);
        verify(platformManager).unpublishTask("Bad", experiment);
    }
}