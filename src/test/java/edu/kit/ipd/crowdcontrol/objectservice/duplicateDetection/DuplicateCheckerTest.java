package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AnswerRatingTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.ImageSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.Shingle;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.StringSimilarity;
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
    private Result<AnswerRecord> answerRecords;
    private Map<AnswerRecord, Integer> answerQualityMap;
    private Set<AnswerRecord> answersWithAssuredQuality;

    @Before
    public void setUp() throws Exception {
        answerQualityMap = new HashMap<>();
        answersWithAssuredQuality = new HashSet<>();
        experimentOperations = mock(ExperimentOperations.class);

        DSLContext create = DSL.using(SQLDialect.MYSQL);
        answerRecords = create.newResult(Tables.ANSWER);
        experimentRecord = create.newRecord(Tables.EXPERIMENT);
        experimentRecord.setIdExperiment(1);
        experimentRecord.setAnswerType(null);
        when(experimentOperations.getExperiment(experimentRecord.getIdExperiment())).thenReturn(Optional.of(experimentRecord));

        answerRatingOperations = mock(AnswerRatingOperations.class);
        when(answerRatingOperations.getAnswersOfExperiment(experimentRecord.getIdExperiment())).thenReturn((Result) answerRecords);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                answerQualityMap.put((AnswerRecord) invocation.getArguments()[0], (Integer) invocation.getArguments()[1]);
                return null;
            }
        }).when(answerRatingOperations).setQualityToAnswer(any(AnswerRecord.class), anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                answersWithAssuredQuality.add((AnswerRecord) invocation.getArguments()[0]);

                return null;
            }
        }).when(answerRatingOperations).setAnswerQualityAssured(any(AnswerRecord.class));


        duplicateChecker = new DuplicateChecker(answerRatingOperations, experimentOperations);


    }

    @After
    public void tearDown() throws Exception {
        experimentRecord = null;
        answerRecords = null;
        answerRatingOperations = null;
        duplicateChecker = null;

    }

    @Ignore
    @Test
    public void testCheckExperimentForDuplicates() throws Exception {
        AnswerRecord uniqueAnswer = new AnswerRecord(0, experimentRecord.getIdExperiment(), "A very different answer", new Timestamp(0), 0,0, 6, false,"",(long)0);

        AnswerRecord originalOne = new AnswerRecord(0, experimentRecord.getIdExperiment(), "This may be a duplicate", new Timestamp(0), 0, 0,6, false,"",(long)0);
        AnswerRecord duplicateOne = new AnswerRecord(0, experimentRecord.getIdExperiment(), "This MAY be a Duplicate toO", new Timestamp(1), 0,0, 6, false,"",(long)0);

        AnswerRecord originalTwo = new AnswerRecord(0, experimentRecord.getIdExperiment(), "An answer should not be similar to other answers, else it is a duplicate", new Timestamp(0), 0,0, 6, false,"",(long)0);
        AnswerRecord duplicateTwo = new AnswerRecord(0, experimentRecord.getIdExperiment(), "an ANSWER shouLd NoT bE similaR to other duplicates, else it is a duplicate", new Timestamp(1), 0,0, 6, false,"",(long)0);

        answerRecords.add(uniqueAnswer);
        answerRecords.add(originalOne);
        answerRecords.add(duplicateOne);
        answerRecords.add(originalTwo);
        answerRecords.add(duplicateTwo);

        answerRecords.forEach(answerRecord -> answerQualityMap.put(answerRecord, answerRecord.getQuality()));
        Map<AnswerRecord, Long> mappingOfAnswersHashes = new HashMap<>();
        answerRecords.forEach(answerRecord -> mappingOfAnswersHashes.put(answerRecord, StringSimilarity.computeSimhashFromShingles(Shingle.getShingle(answerRecord.getAnswer(), 3))));
        duplicateChecker.processDuplicatesOfExperiment(0);

        assertEquals((int) answerQualityMap.get(duplicateOne), 0);
        assertEquals((int) answerQualityMap.get(originalOne), 6);
        assertEquals((int) answerQualityMap.get(uniqueAnswer), 6);
        assertEquals((int) answerQualityMap.get(originalTwo), 6);
        assertEquals((int) answerQualityMap.get(duplicateTwo), 0);

        assertEquals((int) answersWithAssuredQuality.size(), 2);


    }

    @Ignore
    @Test
    public void testTermination() throws Exception {
        AnswerRecord uniqueAnswer = new AnswerRecord(0, experimentRecord.getIdExperiment(), "A very different answer", new Timestamp(0), 0,0, 6, false,"",(long)0);
        answerRecords.add(uniqueAnswer);
        answerRecords.forEach(answerRecord -> answerQualityMap.put(answerRecord, answerRecord.getQuality()));

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


        answerRecords.add(answerRecord1);
        answerRecords.add(answerRecord2);
        answerRecords.add(answerRecord2Duplicate);
        answerRecords.add(answerRecordMalformedURL);

        answerRecords.forEach(answerRecord -> answerQualityMap.put(answerRecord, answerRecord.getQuality()));


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
        mapOfNonDuplicateAnswers.put(answerRecord1, ImageSimilarity.getImageHash(image));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord2, new ArrayList<>()));
        try {


            url = new URL(answerRecord2.getAnswer());
            image = ImageIO.read(url);
        } catch (Exception e) {
            return;
        }
        mapOfNonDuplicateAnswers.put(answerRecord2, ImageSimilarity.getImageHash(image));
        EventManager.ANSWER_CREATE.emit(AnswerRatingTransformer.toAnswerProto(answerRecord2Duplicate, new ArrayList<>()));


        // assertEquals((int)answerQualityMap.get(answerRecordMalformedURL),0);  Cannot test that, because the AnswerRatingTransform creates a different object
        assertEquals(answerQualityMap.get(answerRecord1), answerRecord1.getQuality());
        assertEquals(answerQualityMap.get(answerRecord2), answerRecord2.getQuality());
        //assertEquals((int)answerQualityMap.get(answerRecord2Duplicate), 0);  Cannot test that, because the AnswerRatingTransform creates a different object

        duplicateChecker.terminate();

    }
}