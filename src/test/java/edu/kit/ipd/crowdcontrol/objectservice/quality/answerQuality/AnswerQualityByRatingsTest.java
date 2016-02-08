package edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AlgorithmAnswerQualityParamRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lucaskrauss at 25.01.2016
 */
public class AnswerQualityByRatingsTest {


    final static int maxQuality = 9;
    final static int minQuality = 0;
    AnswerQualityByRatings identifier;
    AnswerRatingOperations answerRatingOperations;
    Result<RatingRecord> ratings;
    Map<AlgorithmAnswerQualityParamRecord, String> params;
    AnswerRecord answer;
    int ratingThreshold;

    @Before
    public void setUp() throws Exception {

        DSLContext create = DSL.using(SQLDialect.MYSQL);
        ratings = create.newResult(Tables.RATING);
        identifier = new AnswerQualityByRatings();
        answer = new AnswerRecord(1, 0, null, null, 0, -1, false);
        params = new HashMap<>();
        params.put(new AlgorithmAnswerQualityParamRecord(null, AnswerQualityByRatings.PARAM_DESCRIPTION, AnswerQualityByRatings.REGEX, AnswerQualityByRatings.algorithmName, AnswerQualityByRatings.PARAMETER_ID), String.valueOf(ratingThreshold));
        ratingThreshold = 6;
        answerRatingOperations = mock(AnswerRatingOperations.class);
        when(answerRatingOperations.getGoodRatingsOfAnswer(Mockito.any(AnswerRecord.class), Mockito.anyInt())).thenReturn(ratings);

    }


    @After
    public void tearDown() throws Exception {
        identifier = null;
        answer = null;
        ratings = null;

    }

    /**
     * Testing behavior with illegal params.
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIllegalRating() {
        ratings.add(new RatingRecord(0, 0, 0, null, -1, "", 0, 0));
        identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality);
    }

    /**
     * Testing if result of the operation is equal to its average
     */
    @Test
    public void answerQualityIsAverageOfRatings() {

        ratings.add(new RatingRecord(0, 0, 0, null, 0, "", 0, ratingThreshold + 1));
        ratings.add(new RatingRecord(0, 0, 0, null, 2, "", 0, ratingThreshold + 1));
        ratings.add(new RatingRecord(0, 0, 0, null, 8, "", 0, ratingThreshold + 1));
        ratings.add(new RatingRecord(0, 0, 0, null, 6, "", 0, ratingThreshold + 1));

        int answerQuality = identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.QUALITY);
        assertEquals(ratings.size(), (int) identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.NUM_OF_RATINGS));

        assertEquals(answerQuality, 4);
    }

    /**
     * Testing qualityIdentification with random ratings
     */
    @Test
    public void testAnswerQualityRnd() {

        Random rand = new Random(181783497276652981L);
        for (int j = 1; j < 30; j++) {
            ratings.clear();
            for (int i = 0; i < j; i++) {
                ratings.add(new RatingRecord(0, 0, 0, null, rand.nextInt(9), "", 0, ratingThreshold + 1));
            }


            int answerQuality = identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.QUALITY);

            int actual = 0;
            for (RatingRecord rating :
                    ratings) {
                actual += rating.getRating();

            }
            actual = (int) Math.round(actual / (double) ratings.size());
            assertEquals(actual, answerQuality);
            assertEquals(ratings.size(), (int) identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.NUM_OF_RATINGS));
        }
    }


    /**
     * Testing behavior with zero-only ratings
     */
    @Test
    public void testZeroQualityRating() {
        ratings.add(new RatingRecord(0, 0, 0, null, 0, "", 0, ratingThreshold + 1));
        ratings.add(new RatingRecord(0, 0, 0, null, 0, "", 0, ratingThreshold + 1));

        assertEquals(0, (int) identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.QUALITY));
    }

    /**
     * Testing behavior without ratings
     */
    @Test
    public void testQualityWithoutRating() {
        assertEquals(ratings.size(), (int) identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.NUM_OF_RATINGS));
        assertEquals((int) answer.getQuality(), (int) identifier.identifyAnswerQuality(answerRatingOperations, answer, params, maxQuality, minQuality).get(AnswerQualityStrategy.QUALITY));
    }
}