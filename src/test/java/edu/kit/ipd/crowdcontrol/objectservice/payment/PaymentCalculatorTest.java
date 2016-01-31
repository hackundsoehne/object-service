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
    Map<WorkerRecord,Set<AnswerRecord>> workerAnswerMap;
    Map<WorkerRecord,Set<RatingRecord>> workerRatingMap;
    PaymentCalculator calculator;

    @Before
    public void setUp() throws Exception {


        workerAnswerMap = new HashMap<>();
        workerRatingMap = new HashMap<>();

        exp = Experiment.newBuilder().setId(13).setPaymentAnswer(10).setPaymentRating(10).setPaymentBase(5).build();


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

    /**
     * Tests payment calculation for answers and ratings of different workers.
     * @throws Exception
     */
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

        Map<Worker,Integer> resultAnswers = calculator.estimatePayment(exp);
        assertNotNull(resultAnswers);
        assertEquals(resultAnswers.size(),2);

        List<Worker> sortedResult = sortWorkerMap(resultAnswers);

        assertEquals((int)resultAnswers.get(sortedResult.get(0)), exp.getPaymentBase()+(exp.getPaymentAnswer()*answerSetWorkerOne.size()));
        assertEquals((int)resultAnswers.get(sortedResult.get(1)), exp.getPaymentBase()+(exp.getPaymentAnswer()*answerSetWorkerTwo.size()));


        Set<RatingRecord> ratingSetWorkerTwo = new HashSet<>();
        ratingSetWorkerTwo.add(new RatingRecord(0,exp.getId(),0,null,6,1,7));
        ratingSetWorkerTwo.add(new RatingRecord(1,exp.getId(),0,null,5,1,9));

        workerRatingMap.put(workerTwo,ratingSetWorkerTwo);


        Map<Worker,Integer> resultRatings = calculator.estimatePayment(exp);
        assertNotNull(resultRatings);
        assertEquals(resultRatings.size(),2);
        sortedResult = sortWorkerMap(resultAnswers);
        assertEquals((int)resultRatings.get(sortedResult.get(0)),  exp.getPaymentBase()+(exp.getPaymentAnswer()*answerSetWorkerOne.size()));
        assertEquals((int)resultRatings.get(sortedResult.get(1)), exp.getPaymentBase()+(exp.getPaymentAnswer()*answerSetWorkerTwo.size())+(exp.getPaymentRating()*ratingSetWorkerTwo.size()));










    }

    @Test (expected = IllegalArgumentException.class)
    public void testFalsePaymentArgs(){
        exp = Experiment.newBuilder().setPaymentBase(-1).setPaymentAnswer(4).setPaymentRating(4).build();
        calculator.estimatePayment(exp);

    }


    private List<Worker> sortWorkerMap(Map<Worker,Integer> map){
        List<Worker> sortedWorkers = new LinkedList<>(map.keySet());


        sortedWorkers.sort((o1, o2) -> Integer.compare(map.get(o2),map.get(o1)));
        return sortedWorkers;
    }
}