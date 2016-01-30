package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lucaskrauss at 30.01.2016
 */
public class PaymentCalculatorTest {

    AnswerRatingOperations ops;
    Experiment exp;
    Map<WorkerRecord,Set<AnswerRecord>> workerAnswerMap;
    Map<WorkerRecord,Set<RatingRecord>> workerRatingMap;
    PaymentCalculator calculator;

    @Before
    public void setUp() throws Exception {


        workerAnswerMap = new HashMap<>();
        workerRatingMap = new HashMap<>();

        exp = mock(Experiment.class);
        when(exp.getId()).thenReturn(13);
        when(exp.getPaymentAnswer()).thenReturn(10);
        when(exp.getPaymentRating()).thenReturn(10);
        when(exp.getPaymentBase()).thenReturn(5);

        ops = mock(AnswerRatingOperations.class);
        when(ops.getGoodAnswersOfExperiment(exp.getId(),0)).thenReturn(workerAnswerMap);
        when(ops.getGoodRatingsOfExperiment(exp.getId(),0)).thenReturn(workerRatingMap);

        calculator = new PaymentCalculator(ops);
    }

    @After
    public void tearDown() throws Exception {
        exp = null;
        ops = null;
        workerRatingMap = null;
        workerAnswerMap = null;

    }

    @Test
    public void testEstimatePayment() throws Exception {

        WorkerRecord workerOne = new WorkerRecord(0,"","","");
        WorkerRecord workerTwo = new WorkerRecord(1,"","","");

        Set<AnswerRecord> answerSetWorkerOne = new HashSet<>();
        answerSetWorkerOne.add(new AnswerRecord(0,workerOne.getIdWorker(),"",null,0,9,true));
        answerSetWorkerOne.add(new AnswerRecord(1,workerOne.getIdWorker(),"",null,0,9,true));
        answerSetWorkerOne.add(new AnswerRecord(2,workerOne.getIdWorker(),"",null,0,9,true));
        answerSetWorkerOne.add(new AnswerRecord(3,workerOne.getIdWorker(),"",null,0,9,true));
        answerSetWorkerOne.add(new AnswerRecord(4,workerOne.getIdWorker(),"",null,0,9,true));

        Set<AnswerRecord> answerSetWorkerTwo = new HashSet<>();
        answerSetWorkerTwo.add(new AnswerRecord(0,workerTwo.getIdWorker(),"",null,0,9,true));
        answerSetWorkerTwo.add(new AnswerRecord(1,workerTwo.getIdWorker(),"",null,0,9,true));


        workerAnswerMap.put(workerOne,answerSetWorkerOne);
        workerAnswerMap.put(workerTwo,answerSetWorkerTwo);

        Map<Worker,Integer> result = calculator.estimatePayment(exp);
        List<Worker> sortedResult = new LinkedList<>();
        for (Worker worker: result.keySet()) {
            assertNotEquals((int)result.get(worker),0);


        }


        assertNotNull(result);







    }
}