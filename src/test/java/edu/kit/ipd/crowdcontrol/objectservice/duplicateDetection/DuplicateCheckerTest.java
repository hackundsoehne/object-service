package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by lucaskrauss at 06.02.2016
 */
public class DuplicateCheckerTest {

    private AnswerRatingOperations answerRatingOperations;
    private ExperimentOperations experimentOperations;
    private Experiment experiment;
    private DuplicateChecker duplicateChecker;
    private Result<AnswerRecord> answerRecords;
    private Map<AnswerRecord,Integer> answerQualityMap;
    private Set<AnswerRecord> answersWithAssuredQuality;

    @Before
    public void setUp() throws Exception {
        answerQualityMap = new HashMap<>();
        answersWithAssuredQuality = new HashSet<>();
        experimentOperations = mock(ExperimentOperations.class);

        DSLContext create = DSL.using(SQLDialect.MYSQL);
        answerRecords = create.newResult(Tables.ANSWER);


        experiment = Experiment.newBuilder().setId(1).build();

        answerRatingOperations = mock(AnswerRatingOperations.class);
        when(answerRatingOperations.getAnswersOfExperiment(experiment.getId())).thenReturn((Result)answerRecords);

        doAnswer(new Answer() {
                     @Override
                     public Object answer(InvocationOnMock invocation) throws Throwable {
                         answerQualityMap.put((AnswerRecord) invocation.getArguments()[0], (Integer) invocation.getArguments()[1]);
                         return null;
                     }
                 }).when(answerRatingOperations).setQualityToAnswer(any(AnswerRecord.class),anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                answersWithAssuredQuality.add((AnswerRecord) invocation.getArguments()[0]);

                return null;
            }
        }).when(answerRatingOperations).setAnswerQualityAssured(any(AnswerRecord.class));



        duplicateChecker = new DuplicateChecker(answerRatingOperations,experimentOperations);




    }

    @After
    public void tearDown() throws Exception {
        experiment = null;
        answerRecords = null;
        answerRatingOperations = null;
        duplicateChecker = null;

    }
    @Ignore
    @Test
    public void testCheckExperimentForDuplicates() throws Exception {
        AnswerRecord uniqueAnswer = new AnswerRecord(0,experiment.getId(),"A very different answer",new Timestamp(0),0,6,false);

        AnswerRecord originalOne = new AnswerRecord(0,experiment.getId(),"This may be a duplicate",new Timestamp(0),0,6,false);
        AnswerRecord duplicateOne = new AnswerRecord(0,experiment.getId(),"This MAY be a Duplicate toO",new Timestamp(1),0,6,false);

        AnswerRecord originalTwo = new AnswerRecord(0,experiment.getId(),"An answer should not be similar to other answers, else it is a duplicate",new Timestamp(0),0,6,false);
        AnswerRecord duplicateTwo = new AnswerRecord(0,experiment.getId(),"an ANSWER shouLd NoT bE similaR to other duplicates, else it is a duplicate",new Timestamp(1),0,6,false);

        answerRecords.add(uniqueAnswer);
        answerRecords.add(originalOne);
        answerRecords.add(duplicateOne);
        answerRecords.add(originalTwo);
        answerRecords.add(duplicateTwo);

        answerRecords.forEach(answerRecord -> answerQualityMap.put(answerRecord,answerRecord.getQuality()));

    //    duplicateChecker.checkExperimentForDuplicates(ExperimentTransformer.toRecord(experiment));

        assertEquals((int)answerQualityMap.get(duplicateOne) , 0);
        assertEquals((int)answerQualityMap.get(originalOne) , 6);
        assertEquals((int)answerQualityMap.get(uniqueAnswer),6);
        assertEquals((int)answerQualityMap.get(originalTwo), 6);
        assertEquals((int)answerQualityMap.get(duplicateTwo), 0);

        assertEquals((int) answersWithAssuredQuality.size(), 2);








    }
}