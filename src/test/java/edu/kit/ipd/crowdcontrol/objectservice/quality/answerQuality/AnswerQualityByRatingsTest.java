package edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by lucaskrauss at 25.01.2016
 */
public class AnswerQualityByRatingsTest {


    final static int maxQuality = 9;
    final static int minQuality = 0;
    AnswerQualityByRatings identifier;
    List<RatingRecord> ratings;
    AnswerRecord answer;

    @Before
    public void setUp() throws Exception {

        identifier = new AnswerQualityByRatings();
        answer = new AnswerRecord(1,0,null,null,0,-1);
        ratings = new LinkedList<>();

    }

    @After
    public void tearDown() throws Exception {
        identifier = null;
        answer = null;
        ratings = null;

    }


    @Test (expected = IllegalArgumentException.class)
    public void throwExceptionIllegalRating(){
        ratings.add(new RatingRecord(0,0,0,null,-1,0,0));
        identifier.identifyAnswerQuality(answer,ratings, maxQuality,minQuality);
    }

    @Test
    public void answerQualityIsAverageOfRatings(){

        ratings.add(new RatingRecord(0,0,0,null,0,0,0));
        ratings.add(new RatingRecord(0,0,0,null,2,0,0));
        ratings.add(new RatingRecord(0,0,0,null,8,0,0));
        ratings.add(new RatingRecord(0,0,0,null,6,0,0));

        int answerQuality = identifier.identifyAnswerQuality(answer,ratings,maxQuality,minQuality);

        assertEquals(answerQuality,4);






    }
}