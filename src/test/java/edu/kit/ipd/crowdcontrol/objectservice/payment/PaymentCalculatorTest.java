package edu.kit.ipd.crowdcontrol.objectservice.payment;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lucaskrauss at 30.01.2016
 */
public class PaymentCalculatorTest {

    AnswerRatingOperations ops;
    Experiment exp;
    Map<WorkerRecord, Integer> workerAnswerMap;
    Map<WorkerRecord, Integer> workerRatingMap;
    PaymentCalculator calculator;

    @Before
    public void setUp() throws Exception {


        workerAnswerMap = new HashMap<>();
        workerRatingMap = new HashMap<>();

        exp = Experiment.newBuilder().setId(13).setPaymentAnswer(10).setPaymentRating(8).setPaymentBase(5).build();


        ops = mock(AnswerRatingOperations.class);
        when(ops.getNumOfGoodAnswersOfExperiment(exp.getId(), 0)).thenReturn(workerAnswerMap);
        when(ops.getNumOfGoodRatingsOfExperiment(exp.getId(), 0)).thenReturn(workerRatingMap);

        calculator = new PaymentCalculator(ops);
    }

    @After
    public void tearDown() throws Exception {
        exp = null;
        ops = null;
        workerRatingMap = null;
        workerAnswerMap = null;

    }

    /**
     * Tests payment calculation for answers and ratings of different workers.
     *
     * @throws Exception
     */
    @Test
    public void testEstimatePayment() throws Exception {

        WorkerRecord workerOne = new WorkerRecord(0, "", "", "", 0);
        WorkerRecord workerTwo = new WorkerRecord(1, "", "", "", 0);

        workerAnswerMap.put(workerOne, 5);
        workerAnswerMap.put(workerTwo, 2);

        Map<Worker, Integer> resultAnswers = calculator.estimatePayment(exp);
        assertNotNull(resultAnswers);


        SortedMap<Worker, Integer> sortedResult = sortWorkerMap(resultAnswers);


        assertEquals((int) sortedResult.get(sortedResult.firstKey()), exp.getPaymentBase() + (exp.getPaymentAnswer() * workerAnswerMap.get(workerOne)));
        assertEquals((int) sortedResult.get(sortedResult.lastKey()), exp.getPaymentBase() + (exp.getPaymentAnswer() * workerAnswerMap.get(workerTwo)));

        workerRatingMap.put(workerOne,0);
        workerRatingMap.put(workerTwo, 2);
        Map<Worker, Integer> resultRatings = calculator.estimatePayment(exp);

        assertNotNull(resultRatings);

        sortedResult = sortWorkerMap(resultRatings);


        assertEquals((int) sortedResult.get(sortedResult.firstKey()), exp.getPaymentBase() + (exp.getPaymentAnswer() * workerAnswerMap.get(workerOne) + (exp.getPaymentRating() * workerRatingMap.get(workerOne))));
        assertEquals((int) sortedResult.get(sortedResult.lastKey()), exp.getPaymentBase() + (exp.getPaymentAnswer() * workerAnswerMap.get(workerTwo) + (exp.getPaymentRating() * workerRatingMap.get(workerTwo))));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testFalsePaymentArgs() {
        exp = Experiment.newBuilder().setPaymentBase(-1).setPaymentAnswer(4).setPaymentRating(4).build();
        calculator.estimatePayment(exp);

    }


    private SortedMap<Worker, Integer> sortWorkerMap(Map<Worker, Integer> map) {
        SortedMap<Worker, Integer> sortedWorkers = new TreeMap<>((o1, o2) -> Integer.compare(map.get(o2), map.get(o1)));
        sortedWorkers.putAll(map);
        return sortedWorkers;
    }
}