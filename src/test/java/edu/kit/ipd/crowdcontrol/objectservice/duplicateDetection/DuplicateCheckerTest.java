package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AnswerRatingTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.HashSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by lucaskrauss at 06.02.2016
 */
public class DuplicateCheckerTest {

    private AnswerRatingOperations answerRatingOperations;
    private ExperimentOperations experimentOperations;
    private ExperimentRecord experimentRecord;
    private DuplicateChecker duplicateChecker;
    private EventManager eventManager;
    private Map<AnswerRecord, Integer> answerQualityMap; //Maps answerID to answerQuality
    private Map<AnswerRecord, Long> answerHashMap;
    private Map<AnswerRecord, String> answerResponseMap; //Maps answerID to answerResponse
    private Map<Integer, AnswerRecord> answerRecordMap;


    @Before
    public void setUp() throws Exception {
        eventManager = new EventManager();
        answerQualityMap = new HashMap<>();
        answerResponseMap = new HashMap<>();
        answerHashMap = new HashMap<>();

        answerRecordMap = new HashMap<>();
        DSLContext create = DSL.using(SQLDialect.MYSQL);
        experimentRecord = create.newRecord(Tables.EXPERIMENT);
        experimentRecord.setIdExperiment(1);
        experimentRecord.setAnswerType(null);

        experimentOperations = mock(ExperimentOperations.class);
        when(experimentOperations.getExperiment(experimentRecord.getIdExperiment())).thenReturn(Optional.of(experimentRecord));


        //--------------------answerRatingOperation-setup------------------------------------
        answerRatingOperations = mock(AnswerRatingOperations.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getDuplicate((long) invocation.getArguments()[0], (double) invocation.getArguments()[2]);
            }
        }).when(answerRatingOperations).getDuplicates(anyLong(), anyInt(), anyDouble());


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AnswerRecord answerRecord = (AnswerRecord) invocation.getArguments()[0];

                answerResponseMap.put(answerRecord, answerRecord.getSystemResponse());
                answerHashMap.put(answerRecord, answerRecord.getHash());
                answerQualityMap.put(answerRecord, answerRecord.getQuality());
                return null;
            }
        }).when(answerRatingOperations).updateAnswer(any(AnswerRecord.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AnswerRecord answerRecord = answerRecordMap.get((int) invocation.getArguments()[0]);
                return Optional.of(answerRecord);
            }
        }).when(answerRatingOperations).getAnswer(anyInt());

        duplicateChecker = new DuplicateChecker(answerRatingOperations, experimentOperations, eventManager);


    }

    @After
    public void tearDown() throws Exception {
        experimentRecord = null;
        answerQualityMap = null;
        answerHashMap = null;
        answerResponseMap = null;
        answerRatingOperations = null;
        duplicateChecker.terminate();
        duplicateChecker = null;

    }

    @Test
    public void testCheckExperimentForDuplicates() throws Exception {
        AnswerRecord uniqueAnswer = new AnswerRecord(0, experimentRecord.getIdExperiment(), "A very different answer", new Timestamp(0), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord sim1A = new AnswerRecord(1, experimentRecord.getIdExperiment(), "This may be a duplicate", new Timestamp(0), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord sim1B = new AnswerRecord(2, experimentRecord.getIdExperiment(), "This MAY be a Duplicate toO", new Timestamp(1), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord sim2A = new AnswerRecord(3, experimentRecord.getIdExperiment(), "An answer should not be similar to other answers, else it is a duplicate", new Timestamp(0), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord sim2B = new AnswerRecord(4, experimentRecord.getIdExperiment(), "an ANSWER shouLd NoT bE similaR to other duplicates, else it is a duplicate", new Timestamp(1), 0, 0, 6, false, "", (long) 0, false);


        answerRecordMap.put(uniqueAnswer.getIdAnswer(), uniqueAnswer);
        answerRecordMap.put(sim1A.getIdAnswer(), sim1A);
        answerRecordMap.put(sim1B.getIdAnswer(), sim1B);
        answerRecordMap.put(sim2A.getIdAnswer(), sim2A);
        answerRecordMap.put(sim2B.getIdAnswer(), sim2B);

        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(uniqueAnswer, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim1A, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim1B, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim2A, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim2B, new ArrayList<>()));


        Thread.sleep(500);
        assertEquals(uniqueAnswer.getQuality(), answerQualityMap.get(uniqueAnswer));
        assertEquals(sim1A.getQuality(), answerQualityMap.get(sim1A));
        assertEquals((int) sim1B.getQuality(), 0);
        assertEquals(sim2A.getQuality(), answerQualityMap.get(sim1A));
        assertEquals((int) sim2B.getQuality(), 0);

        assertEquals(sim1B.getSystemResponse(), DuplicateChecker.DUPLICATE_RESPONSE);
        assertEquals(sim2B.getSystemResponse(), DuplicateChecker.DUPLICATE_RESPONSE);

    }


    @Test
    public void testTermination() throws Exception {
        answerResponseMap = new HashMap<>();
        answerQualityMap = new HashMap<>();
        AnswerRecord uniqueAnswer = new AnswerRecord(0, experimentRecord.getIdExperiment(), "A very different answer", new Timestamp(0), 0, 0, 6, false, "", (long) 0, false);
        answerRecordMap.put(uniqueAnswer.getIdAnswer(), uniqueAnswer);
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(uniqueAnswer, new ArrayList<>()));
        Thread.sleep(1000);
        assertTrue(duplicateChecker.terminate());
    }

    @Test
    public void testImageDuplicateDetection() throws Exception {

        experimentRecord.setAnswerType("picture");
        AnswerRecord answerRecord1 = new AnswerRecord(0, experimentRecord.getIdExperiment(), "http://www.franz-sales-verlag.de/fsvwiki/uploads/Lexikon/Baum.jpg", new Timestamp(0), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord answerRecord2 = new AnswerRecord(1, experimentRecord.getIdExperiment(), "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg/450px-Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg", new Timestamp(1), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord answerRecord2Duplicate = new AnswerRecord(2, experimentRecord.getIdExperiment(), "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg/450px-Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg", new Timestamp(4), 0, 0, 6, false, "", (long) 0, false);
        AnswerRecord answerRecordMalformedURL = new AnswerRecord(3, experimentRecord.getIdExperiment(), "htt214ljbaq    dlkjps://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg/450px-Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg", new Timestamp(1), 0, 0, 6, false, "", (long) 0, false);

        answerRecordMap.put(answerRecord1.getIdAnswer(), answerRecord1);
        answerRecordMap.put(answerRecord2.getIdAnswer(), answerRecord2);
        answerRecordMap.put(answerRecord2Duplicate.getIdAnswer(), answerRecord2Duplicate);
        answerRecordMap.put(answerRecordMalformedURL.getIdAnswer(), answerRecordMalformedURL);

        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecordMalformedURL, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord1, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord2, new ArrayList<>()));
        eventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord2Duplicate, new ArrayList<>()));

        Thread.sleep(5000);

        assertEquals(answerRecord1.getQuality(), answerQualityMap.get(answerRecord1));
        assertEquals(answerRecord2.getQuality(), answerQualityMap.get(answerRecord2));
        assertEquals((int) answerQualityMap.get(answerRecord2Duplicate), 0);
        assertEquals((int) answerQualityMap.get(answerRecordMalformedURL), 0);

        assertEquals(answerRecord2Duplicate.getSystemResponse(), DuplicateChecker.DUPLICATE_RESPONSE);
        assertEquals(answerRecordMalformedURL.getSystemResponse(), DuplicateChecker.URL_MALFORMED_RESPONSE);
    }

    private List<AnswerRecord> getDuplicate(long hash, double threshold) {
        List<AnswerRecord> duplicates = new ArrayList<>();
        for (Map.Entry<AnswerRecord, Long> entry : answerHashMap.entrySet()) {
            if (HashSimilarity.getSimilarityFromHash(hash, entry.getValue()) > threshold) {
                duplicates.add(entry.getKey());
            }
        }
        return duplicates;
    }
}
