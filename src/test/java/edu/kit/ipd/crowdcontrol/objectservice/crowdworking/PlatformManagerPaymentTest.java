package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ExperimentsPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by marcel on 16.03.16.
 */
public class PlatformManagerPaymentTest {

    private PlatformManager platformManager;
    private ExperimentsPlatformOperations experimentsPlatformOperations;
    private DummyPlatform dummyPlatform;
    private PlatformOperations platformOperations;
    private WorkerOperations workerOperations;
    private List<WorkerRecord> workerRecordList;
    private Experiment experiment;
    @Before
    public void setUp() throws Exception {
        dummyPlatform = mock(DummyPlatform.class);
        when(dummyPlatform.getID()).thenReturn("bla");
        when(dummyPlatform.getPayment()).thenReturn(Optional.of(dummyPlatform));
        when(dummyPlatform.getWorker()).thenReturn(Optional.empty());
        when(dummyPlatform.getName()).thenReturn("Platform");

        experimentsPlatformOperations = mock(ExperimentsPlatformOperations.class);
        platformOperations = mock(PlatformOperations.class);
        workerOperations = mock(WorkerOperations.class);

        platformManager = new PlatformManager(Collections.singletonList(dummyPlatform),
                null,
                null,
                experimentsPlatformOperations,
                platformOperations,
                workerOperations);

        workerRecordList = new ArrayList<>();
        workerRecordList.add(new WorkerRecord(0, new JsonPrimitive(""), dummyPlatform.getID(), "olaf@example.com", 20, "olaf"));
        workerRecordList.add(new WorkerRecord(1, new JsonPrimitive(""), dummyPlatform.getID(), "gunther@example.com", 20, "günther"));
        experiment = Experiment.newBuilder().setId(0).build();

        when(workerOperations.getWorkerWithWork(experiment.getId(), dummyPlatform.getID())).thenReturn(workerRecordList);
        ExperimentsPlatformRecord experimentsPlatformRecord = new ExperimentsPlatformRecord(0, 1, dummyPlatform.getID(), null, "");
        when(experimentsPlatformOperations.getExperimentsPlatform(dummyPlatform.getID(),experiment.getId())).thenReturn(Optional.of(experimentsPlatformRecord));

    }

    @Test
    public void testPayExperiment() throws Exception {
        List<PaymentJob> paymentJobs = new ArrayList<>();
        paymentJobs.add(new PaymentJob(workerRecordList.get(0), 20, ""));
        paymentJobs.add(new PaymentJob(workerRecordList.get(1), 20, ""));

        platformManager.payExperiment(dummyPlatform.getID(),experiment, paymentJobs);

        verify(dummyPlatform).payExperiment(eq(0), anyObject(), eq(experiment), eq(paymentJobs));
    }

    /**
     * @link http://stackoverflow.com/a/6527990/2373138
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void testPayExperimentFail() throws Exception {
        expectedException.expect(PreActionException.class);
        expectedException.expectCause(instanceOf(IllegalWorkerSetException.class));

        List<PaymentJob> paymentJobs = new ArrayList<>();
        paymentJobs.add(new PaymentJob(workerRecordList.get(0), 20, ""));

        platformManager.payExperiment(dummyPlatform.getID(),experiment, paymentJobs);

        verify(dummyPlatform).payExperiment(eq(0), anyObject(), eq(experiment), eq(paymentJobs));
    }
}