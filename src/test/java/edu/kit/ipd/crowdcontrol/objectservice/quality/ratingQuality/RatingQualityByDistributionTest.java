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
    final static int minQuqlity = 0;


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
    public void throwException(){
       ratings.add(new RatingRecord(-1,0,0,null,-1,0,-1));
       identifier.identifyRatingQuality(ratings, maxQuality,minQuqlity);

    }


    @Test
    public void optimalRating(){
        ratings.add(new RatingRecord(1,0,0,null,0,0,0));
        ratings.add(new RatingRecord(1,0,0,null,9,0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,0,0));
        RatingRecord ratingRecord = new RatingRecord(1,0,0,null,3,0,0);
        ratings.add(ratingRecord);


        Map<RatingRecord,Integer> map = identifier.identifyRatingQuality(ratings,maxQuality,minQuqlity);

        assertEquals((int)map.get(ratingRecord),maxQuality);
    }
}