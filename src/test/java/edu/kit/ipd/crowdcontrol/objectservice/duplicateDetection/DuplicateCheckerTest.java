package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AnswerRatingTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.HashSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.ImageSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
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




    private Map<Integer, Integer> answerQualityMap; //Maps answerID to answerQuality
    private Map<AnswerRecord,   Long> answerHashMap;
    private Map<Integer, String> answerResponseMap; //Maps answerID to answerResponse
    private List<Integer> answerWithQualityAssuredList;
    @Before
    public void setUp() throws Exception {
        answerQualityMap = new HashMap<>();
        answerResponseMap = new HashMap<>();
        answerResponseMap = new HashMap<>();
        answerWithQualityAssuredList = new ArrayList<>();

        DSLContext create = DSL.using(SQLDialect.MYSQL);



        answerHashMap = new HashMap<>();

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

                return getDuplicate((long)invocation.getArguments()[0],(double)invocation.getArguments()[2]);
            }
        }).when(answerRatingOperations).getDuplicates(anyLong(),anyInt(),anyDouble());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AnswerRecord answerRecord = (AnswerRecord)invocation.getArguments()[0];
                answerQualityMap.put(answerRecord.getIdAnswer(),(int)invocation.getArguments()[1]);
                return null;
            }
        }).when(answerRatingOperations).setQualityToAnswer(any(AnswerRecord.class), anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AnswerRecord answerRecord = (AnswerRecord) invocation.getArguments()[0];
                answerRecord.setQualityAssured(true);
                answerWithQualityAssuredList.add(answerRecord.getIdAnswer());
                 return null;
            }
        }).when(answerRatingOperations).setAnswerQualityAssured(any(AnswerRecord.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AnswerRecord answerRecord = (AnswerRecord) invocation.getArguments()[0];
                answerResponseMap.put(answerRecord.getIdAnswer(),(String) invocation.getArguments()[1]);
                return null;
            }
        }).when(answerRatingOperations).setSystemResponseField(any(AnswerRecord.class), anyString());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                answerHashMap.put((AnswerRecord)invocation.getArguments()[0], (long) invocation.getArguments()[1]);
                return null;
            }
        }).when(answerRatingOperations).setHashToAnswer(any(AnswerRecord.class),anyLong());
        //----------------------------------------------------------------------------




        duplicateChecker = new DuplicateChecker(answerRatingOperations, experimentOperations);


    }

    @After
    public void tearDown() throws Exception {
        experimentRecord = null;
        answerQualityMap = null;
        answerWithQualityAssuredList = null;
        answerHashMap = null;
        answerResponseMap = null;
        answerRatingOperations = null;
        duplicateChecker = null;

    }

    @Ignore
    @Test
    public void testCheckExperimentForDuplicates() throws Exception {
        AnswerRecord uniqueAnswer = new AnswerRecord(0, experimentRecord.getIdExperiment(), "A very different answer", new Timestamp(0), 0,0, 6, false,"",(long)0);

        AnswerRecord sim1A = new AnswerRecord(1, experimentRecord.getIdExperiment(), "This may be a duplicate", new Timestamp(0), 0, 0,6, false,"",(long)0);
        AnswerRecord sim1B = new AnswerRecord(2, experimentRecord.getIdExperiment(), "This MAY be a Duplicate toO", new Timestamp(1), 0,0, 6, false,"",(long)0);

        AnswerRecord sim2A = new AnswerRecord(3, experimentRecord.getIdExperiment(), "An answer should not be similar to other answers, else it is a duplicate", new Timestamp(0), 0,0, 6, false,"",(long)0);
        AnswerRecord sim2B = new AnswerRecord(4, experimentRecord.getIdExperiment(), "an ANSWER shouLd NoT bE similaR to other duplicates, else it is a duplicate", new Timestamp(1), 0,0, 6, false,"",(long)0);

        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(uniqueAnswer,new ArrayList<>()));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim1A,new ArrayList<>()));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim1B,new ArrayList<>()));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim2A,new ArrayList<>()));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(sim2B,new ArrayList<>()));


        Thread.sleep(5000);
        assertTrue(answerHashMap.size() == 5);

        int idDuplicateSim1 = (answerQualityMap.get(sim1A.getIdAnswer()) == null) ? sim1B.getIdAnswer() : sim1A.getIdAnswer(); //Find out duplicate ID
        int idDuplicateSim2 = (answerQualityMap.get(sim2A.getIdAnswer()) == null) ? sim2B.getIdAnswer() : sim2A.getIdAnswer();

        assertEquals((int) answerQualityMap.get(idDuplicateSim1),0);
        assertEquals((int) answerQualityMap.get(idDuplicateSim2),0);

        assertTrue(answerResponseMap.get(idDuplicateSim1).startsWith("This answer is considered a duplicate with:"));
        assertTrue(answerResponseMap.get(idDuplicateSim2).startsWith("This answer is considered a duplicate with:"));

    }

    @Ignore
    @Test
    public void testTermination() throws Exception {
        AnswerRecord uniqueAnswer = new AnswerRecord(0, experimentRecord.getIdExperiment(), "A very different answer", new Timestamp(0), 0,0, 6, false,"",(long)0);

        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(uniqueAnswer, new ArrayList<>()));
        Thread.sleep(500);
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(uniqueAnswer, new ArrayList<>()));
        Thread.sleep(1000);

        assertTrue(duplicateChecker.terminate());

    }

    @Ignore
    @Test
    public void testImageDuplicateDetection() throws Exception {

        Map<AnswerRecord, Long> mapOfNonDuplicateAnswers = new HashMap<>();
        when(answerRatingOperations.getMapOfHashesOfNonDuplicateAnswers(experimentRecord.getIdExperiment())).thenReturn(mapOfNonDuplicateAnswers);
        experimentRecord.setAnswerType("picture");


        AnswerRecord answerRecord1 = new AnswerRecord(0, experimentRecord.getIdExperiment(), "http://www.franz-sales-verlag.de/fsvwiki/uploads/Lexikon/Baum.jpg", new Timestamp(0), 0,0, 6, false,"",(long)0);
        AnswerRecord answerRecord2 = new AnswerRecord(0, experimentRecord.getIdExperiment(), "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg/450px-Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg", new Timestamp(1), 0,0, 6, false,"",(long)0);
        AnswerRecord answerRecord2Duplicate = new AnswerRecord(0, experimentRecord.getIdExperiment(), "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg/450px-Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg", new Timestamp(4), 0,0, 6, false,"",(long)0);
        AnswerRecord answerRecordMalformedURL = new AnswerRecord(0, experimentRecord.getIdExperiment(), "htt214ljbaq    dlkjps://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg/450px-Sequoiadendron_giganteum_at_Kenilworth_Castle.jpg", new Timestamp(1), 0,0, 6, false,"",(long)0);



        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecordMalformedURL, new ArrayList<>()));

        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord1, new ArrayList<>()));

        BufferedImage image;
        URL url;
        try {


            url = new URL(answerRecord1.getAnswer());
            image = ImageIO.read(url);
        } catch (Exception e) {
            return;
        }
        mapOfNonDuplicateAnswers.put(answerRecord1, ImageSimilarity.getImageHashFromGreyScaleDeviation(image));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord2, new ArrayList<>()));
        try {


            url = new URL(answerRecord2.getAnswer());
            image = ImageIO.read(url);
        } catch (Exception e) {
            return;
        }
        mapOfNonDuplicateAnswers.put(answerRecord2, ImageSimilarity.getImageHashFromGreyScaleDeviation(image));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord2Duplicate, new ArrayList<>()));


        // assertEquals((int)answerQualityMap.get(answerRecordMalformedURL),0);  Cannot test that, because the AnswerRatingTransform creates a different object
        assertEquals(answerQualityMap.get(answerRecord1), answerRecord1.getQuality());
        assertEquals(answerQualityMap.get(answerRecord2), answerRecord2.getQuality());
        //assertEquals((int)answerQualityMap.get(answerRecord2Duplicate), 0);  Cannot test that, because the AnswerRatingTransform creates a different object

        duplicateChecker.terminate();

    }

    private AnswerRecord getAnswer(int id){
        for (Map.Entry<AnswerRecord,Long> entry: answerHashMap.entrySet()  ) {
            if(entry.getKey().getIdAnswer() == id){
                return entry.getKey();
            }
        }
        return null;
    }


    private List<AnswerRecord> getDuplicate(long hash, double threshold){
        List<AnswerRecord> duplicates = new ArrayList<>();
        for (Map.Entry<AnswerRecord,Long> entry: answerHashMap.entrySet()) {
            if(HashSimilarity.getSimilarityFromHash(hash,entry.getValue()) > threshold){
                duplicates.add(entry.getKey());
            }
        }
        return duplicates;
    }
}