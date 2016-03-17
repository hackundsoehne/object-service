package edu.kit.ipd.crowdcontrol.objectservice.payment;

import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.feedback.FeedbackCreator;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by lckr on 17/03/16.
 */
public class PaymentDispatcherTest {

    private PlatformManager platformManager;
    private FeedbackCreator feedbackCreator;
    private PaymentDispatcher paymentDispatcher;
    private AnswerRatingOperations answerRatingOperations;
    private WorkerOperations workerOperations;
    private EventManager eventManager;
    private Experiment experiment;
    private Result<WorkerRecord> workerRecords;
    private Map<WorkerRecord,Integer> workerAnswerMap;
    private Map<WorkerRecord,Integer> workerRatingMap;
    private CompletableFuture<Boolean> paymentDone;
    private boolean reachedTargetMethod;


    @Before
    public void setUp() throws Exception {
        this.reachedTargetMethod = false;
        this.paymentDone = new CompletableFuture<>();
        paymentDone.isDone();
        this.workerAnswerMap = new HashMap<>();
        this.workerRatingMap = new HashMap<>();
        DSLContext create = DSL.using(SQLDialect.MYSQL);
        workerRecords = create.newResult(Tables.WORKER);
        this.experiment = Experiment.newBuilder().setId(1).setState(Experiment.State.PUBLISHED).build();
        this.eventManager = new EventManager();
        this.answerRatingOperations = mock(AnswerRatingOperations.class);
        this.workerOperations = mock(WorkerOperations.class);
        this.platformManager = mock(PlatformManager.class);
        this.feedbackCreator = mock(FeedbackCreator.class);
        this.paymentDispatcher = new PaymentDispatcher(feedbackCreator,platformManager,answerRatingOperations,workerOperations,eventManager);
        when(workerOperations.getWorkersOfExp(experiment.getId())).thenReturn(workerRecords);
        when(answerRatingOperations.getNumOfGoodAnswersOfExperiment(experiment.getId(),0)).thenReturn(workerAnswerMap);
        when(answerRatingOperations.getNumOfGoodRatingsOfExperiment(experiment.getId(),0)).thenReturn(workerRatingMap);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                reachedTargetMethod = true;
                return paymentDone;
            }
        }).when(platformManager).payExperiment(any(),any(),any());
    }

    @After
    public void tearDown() throws Exception {
        this.reachedTargetMethod = false;
        eventManager = null;
        answerRatingOperations = null;
        workerOperations = null;
        platformManager = null;
        feedbackCreator = null;
        paymentDispatcher = null;

    }

    @Test
    public void testDispatcher() throws Exception{
        WorkerRecord workerOne = new WorkerRecord(0, new JsonPrimitive(""), "", "", 0, "");
        WorkerRecord workerTwo = new WorkerRecord(1, new JsonPrimitive(""), "", "", 0, "");
        WorkerRecord workerThree = new WorkerRecord(3,new JsonPrimitive(""),"","",0,"");
        workerAnswerMap.put(workerOne, 5);
        workerAnswerMap.put(workerTwo, 2);

        workerRecords.add(workerOne);
        workerRecords.add(workerTwo);
        paymentDone.complete(true);
        eventManager.EXPERIMENT_CHANGE.emit(new ChangeEvent<>(experiment,experiment.toBuilder().setState(Experiment.State.STOPPED).build()));
        assertTrue(reachedTargetMethod);
    }
}