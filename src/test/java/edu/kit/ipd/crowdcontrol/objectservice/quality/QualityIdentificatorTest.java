package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.ExperimentOperator;
import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AlgorithmOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityByRatings;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityByDistribution;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Created by lckr on 17/03/16.
 */
public class QualityIdentificatorTest {

    private AnswerRatingOperations answerRatingOperations;
    private AlgorithmOperations algorithmOperations;
    private ExperimentOperations experimentOperations;
    private ExperimentOperator experimentOperator;
    private EventManager eventManager;
    private ExperimentsPlatformOperations experimentsPlatformOperations;
    private ExperimentFetcher experimentFetcher;

    private AnswerRecord answerRecord;
    private ExperimentRecord experimentRecord;
    private Rating rating;
    private List<AlgorithmAnswerQualityRecord> answerAlgorithms;
    private List<AlgorithmRatingQualityRecord> ratingAlgorithms;
    private Result<RatingRecord> ratingRecords;
    private QualityIdentificator qualityIdentificator;
    private boolean reachedTargetMethod;

    @Before
    public void setUp() throws Exception {

        this.experimentsPlatformOperations = mock(ExperimentsPlatformOperations.class);
        this.experimentFetcher = mock(ExperimentFetcher.class);
        Map<Integer,ExperimentsPlatformStatusPlatformStatus>statuses = new HashMap<>();
        statuses.put(1,ExperimentsPlatformStatusPlatformStatus.running);
        when(experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(anyInt())).thenReturn(statuses);

        this.reachedTargetMethod = false;

        DSLContext context = DSL.using(SQLDialect.MYSQL);
        ratingRecords = context.newResult(Tables.RATING);
        rating = Rating.newBuilder().setExperimentId(0).build();

        this.experimentRecord = new ExperimentRecord();
        AnswerQualityByRatings answerQualityByRatings = new AnswerQualityByRatings();
        RatingQualityByDistribution ratingQualityByDistribution = new RatingQualityByDistribution();
        experimentRecord.setAlgorithmQualityAnswer(answerQualityByRatings.getAlgorithmName());
        experimentRecord.setAlgorithmQualityRating(ratingQualityByDistribution.getAlgorithmName());
        experimentRecord.setIdExperiment(0);
        experimentRecord.setRatingsPerAnswer(1);
        experimentRecord.setNeededAnswers(1);

        this.answerRecord = new AnswerRecord();
        answerRecord.setIdAnswer(1);
        answerRecord.setExperiment(1);
        answerRecord.setDuplicate(true);

        this.eventManager = new EventManager();
        this.algorithmOperations = mock(AlgorithmOperations.class);
        this.answerRatingOperations = mock(AnswerRatingOperations.class);
        this.experimentOperations = mock(ExperimentOperations.class);
        this.experimentOperator = mock(ExperimentOperator.class);

        answerAlgorithms = new ArrayList<>();
        ratingAlgorithms = new ArrayList<>();
        when(answerRatingOperations.getAnswer(anyInt())).thenReturn(Optional.of(answerRecord));
        when(answerRatingOperations.getNumberOfFinalGoodAnswers(anyInt())).thenReturn(1);
        when(answerRatingOperations.getAnswerFromRating(rating)).thenReturn(Optional.of(answerRecord));
        List<RatingRecord> ratingRecordList = new ArrayList<>();
        ratingRecordList.add(new RatingRecord(0,0,0,null,0,1,"",0,0));
        when(answerRatingOperations.getRelatedRatings(answerRecord.getIdAnswer())).thenReturn(ratingRecordList);
        when(experimentOperations.getExperiment(anyInt())).thenReturn(Optional.of(experimentRecord));
        when(answerRatingOperations.getGoodRatingsOfAnswer(any(AnswerRecord.class), anyInt())).thenReturn(ratingRecords);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                reachedTargetMethod = true;
                return null;
            }
        }).when(experimentOperator).endExperiment(any(Experiment.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(answerRatingOperations).setQualityToAnswer(any(AnswerRecord.class), anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(answerRatingOperations).setAnswerQualityAssured(any(AnswerRecord.class));

        Map<AlgorithmRatingQualityParamRecord, String> algorithmRatingQualityParamRecordStringMap = new HashMap<>();
        algorithmRatingQualityParamRecordStringMap.put(new AlgorithmRatingQualityParamRecord(0, ratingQualityByDistribution.getAlgorithmDescription()
                , "", ratingQualityByDistribution.getAlgorithmName(), ""), "");

        when(algorithmOperations.getRatingQualityParams(ratingQualityByDistribution.getAlgorithmName(), experimentRecord.getIdExperiment()))
                .thenReturn(algorithmRatingQualityParamRecordStringMap);


        Map<AlgorithmAnswerQualityParamRecord, String> algorithmAnswerQualityParamRecordStringMap = new HashMap<>();
        algorithmAnswerQualityParamRecordStringMap.put(answerQualityByRatings.getParams().get(0), "2");
        when(algorithmOperations.getAnswerQualityParams(answerQualityByRatings.getAlgorithmName(), experimentRecord.getIdExperiment()))
                .thenReturn(algorithmAnswerQualityParamRecordStringMap);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                answerAlgorithms.add((AlgorithmAnswerQualityRecord) invocation.getArguments()[0]);
                return null;
            }
        }).when(algorithmOperations).storeAnswerQualityAlgorithm(any(AlgorithmAnswerQualityRecord.class), anyList());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ratingAlgorithms.add((AlgorithmRatingQualityRecord) invocation.getArguments()[0]);
                return null;
            }
        }).when(algorithmOperations).storeRatingQualityAlgorithm(any(AlgorithmRatingQualityRecord.class), anyList());


        this.qualityIdentificator = new QualityIdentificator(algorithmOperations, answerRatingOperations, experimentOperations, experimentOperator,experimentsPlatformOperations, eventManager,experimentFetcher);

    }

    @After
    public void tearDown() throws Exception {
        this.ratingAlgorithms = null;
        this.answerAlgorithms = null;
        this.experimentOperator = null;
        this.algorithmOperations = null;
        this.answerRatingOperations = null;
        this.experimentOperations = null;
        this.eventManager = null;
        this.reachedTargetMethod = false;

    }


    @Test
    public void testAlgorithmInsertion() throws Exception {
        assertEquals(ratingAlgorithms.size(), 1);
        assertEquals(answerAlgorithms.size(), 1);
    }

    @Test
    public void testControlFlow() throws Exception {
        RatingRecord ratingRecord = new RatingRecord(0, experimentRecord.getIdExperiment(), answerRecord.getIdAnswer(), null,
                4, 0, "", 0, 1);
        ratingRecords.add(ratingRecord);


        eventManager.RATINGS_CREATE.emit(rating);
        assertTrue(reachedTargetMethod);

    }
}