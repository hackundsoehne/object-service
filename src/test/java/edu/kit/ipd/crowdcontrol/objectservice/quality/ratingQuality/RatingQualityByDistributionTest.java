package edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by lucaskrauss at 25.01.2016
 */
public class RatingQualityByDistributionTest {


    RatingQualityByDistribution identifier;
    List<RatingRecord> ratings ;
    final static int maxQuality = 9;
    final static int minQuality = 0;


    @Before
    public void setUp() throws Exception {
        identifier = new RatingQualityByDistribution();
        ratings = new LinkedList<>();
    }

    @After
    public void tearDown() throws Exception {
        ratings = null;
    }

   @Test (expected = IllegalArgumentException.class)
    public void throwExceptionInvalidRating(){
       ratings.add(new RatingRecord(-1,0,0,null,-1,0,-1));
       identifier.identifyRatingQuality(ratings, maxQuality, minQuality);

    }


    @Test
    public void optimalRatingEqualsMostChosenRating(){
        ratings.add(new RatingRecord(1,0,0,null,0,0,0));
        ratings.add(new RatingRecord(1,0,0,null,9,0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,0,0));
        RatingRecord ratingRecord = new RatingRecord(1,0,0,null,3,0,0);
        ratings.add(ratingRecord);


        Map<RatingRecord,Integer> map = identifier.identifyRatingQuality(ratings,maxQuality, minQuality);

        assertEquals((int)map.get(ratingRecord),maxQuality);
    }


    @Test
    public void optimalRatingEqualsAverageOfMostChosenRatings(){
        RatingRecord r1a = new RatingRecord(1,0,0,null,0,0,0);
        RatingRecord r1b = new RatingRecord(1,0,0,null,0,0,0);

        RatingRecord r2a = new RatingRecord(1,0,0,null,8,0,0);
        RatingRecord r2b = new RatingRecord(1,0,0,null,8,0,0);

        RatingRecord r3 = new RatingRecord(1,0,0,null,4,0,0);


        ratings.add(r1a);
        ratings.add(r1b);
        ratings.add(r2a);
        ratings.add(r2b);
        ratings.add(r3);

        Map<RatingRecord,Integer> map = identifier.identifyRatingQuality(ratings,maxQuality, minQuality);


        assertEquals((int)map.get(r1a),5);

        assertEquals((int)map.get(r2a),5);

        assertEquals((int)map.get(r3),maxQuality);




    }


    @Test (expected = IllegalArgumentException.class)
    public void throwExceptionMissingRatings(){
        identifier.identifyRatingQuality(ratings,maxQuality, minQuality);

    }
}