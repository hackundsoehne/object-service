package edu.kit.ipd.crowdcontrol.objectservice.payment;

import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lucaskrauss at 30.01.2016
 */
public class PaymentCalculatorTest {

    AnswerRatingOperations ops;
    WorkerOperations workerOperations;
    Experiment exp;
    Map<WorkerRecord, Integer> workerAnswerMap;
    Map<WorkerRecord, Integer> workerRatingMap;
    Result<WorkerRecord> workerRecordList ;
    PaymentCalculator calculator;

    @Before
    public void setUp() throws Exception {
        DSLContext create = DSL.using(SQLDialect.MYSQL);


        workerAnswerMap = new HashMap<>();
        workerRatingMap = new HashMap<>();
        workerRecordList = create.newResult(Tables.WORKER);

        exp = Experiment.newBuilder().setId(13).setPaymentAnswer(toProtoInt(10)).setPaymentRating(toProtoInt(8)).setPaymentBase(toProtoInt(5)).build();

        workerOperations = mock(WorkerOperations.class);
        when(workerOperations.getWorkersOfExp(exp.getId())).thenReturn(workerRecordList);
        ops = mock(AnswerRatingOperations.class);
        when(ops.getNumOfGoodAnswersOfExperiment(exp.getId(), 0)).thenReturn(workerAnswerMap);
        when(ops.getNumOfGoodRatingsOfExperiment(exp.getId(), 0)).thenReturn(workerRatingMap);

        calculator = new PaymentCalculator(ops,workerOperations);
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

        WorkerRecord workerOne = new WorkerRecord(0, new JsonPrimitive(""), "", "", 0, "");
        WorkerRecord workerTwo = new WorkerRecord(1, new JsonPrimitive(""), "", "", 0, "");

        workerAnswerMap.put(workerOne, 5);
        workerAnswerMap.put(workerTwo, 2);

        Map<WorkerRecord, Integer> resultAnswers = calculator.estimatePayment(exp);
        assertNotNull(resultAnswers);


        SortedMap<WorkerRecord, Integer> sortedResult = sortWorkerMap(resultAnswers);


        assertEquals((int) sortedResult.get(sortedResult.firstKey()), exp.getPaymentBase().getValue() + (exp.getPaymentAnswer().getValue() * workerAnswerMap.get(workerOne)));
        assertEquals((int) sortedResult.get(sortedResult.lastKey()), exp.getPaymentBase().getValue() + (exp.getPaymentAnswer().getValue() * workerAnswerMap.get(workerTwo)));

        workerRatingMap.put(workerOne,0);
        workerRatingMap.put(workerTwo, 2);
        Map<WorkerRecord, Integer> resultRatings = calculator.estimatePayment(exp);

        assertNotNull(resultRatings);

        sortedResult = sortWorkerMap(resultRatings);


        assertEquals((int) sortedResult.get(sortedResult.firstKey()), exp.getPaymentBase().getValue() + (exp.getPaymentAnswer().getValue() * workerAnswerMap.get(workerOne) + (exp.getPaymentRating().getValue() * workerRatingMap.get(workerOne))));
        assertEquals((int) sortedResult.get(sortedResult.lastKey()), exp.getPaymentBase().getValue() + (exp.getPaymentAnswer().getValue() * workerAnswerMap.get(workerTwo) + (exp.getPaymentRating().getValue() * workerRatingMap.get(workerTwo))));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testFalsePaymentArgs() {
        exp = Experiment.newBuilder().setPaymentBase(toProtoInt(-1)).setPaymentAnswer(toProtoInt(4)).setPaymentRating(toProtoInt(4)).build();
        calculator.estimatePayment(exp);

    }


    private SortedMap<WorkerRecord, Integer> sortWorkerMap(Map<WorkerRecord, Integer> map) {
        SortedMap<WorkerRecord, Integer> sortedWorkers = new TreeMap<>((o1, o2) -> Integer.compare(map.get(o2), map.get(o1)));
        sortedWorkers.putAll(map);
        return sortedWorkers;
    }

    private edu.kit.ipd.crowdcontrol.objectservice.proto.Integer toProtoInt(int x) {
        return edu.kit.ipd.crowdcontrol.objectservice.proto.Integer.newBuilder().setValue(x).build();
    }
}