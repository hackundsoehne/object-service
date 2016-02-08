package edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AlgorithmRatingQualityParamRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by lucaskrauss at 25.01.2016
 */
public class RatingQualityByDistributionTest {


    RatingQualityByDistribution identifier;
    List<RatingRecord> ratings ;
    final static int maxQuality = 9;
    final static int minQuality = 0;
    Map<AlgorithmRatingQualityParamRecord,String> params;


    @Before
    public void setUp() throws Exception {
        identifier = new RatingQualityByDistribution();
        ratings = new LinkedList<>();
        params = new HashMap<>();
    }

    @After
    public void tearDown() throws Exception {
        ratings = null;
    }

   @Test (expected = IllegalArgumentException.class)
    public void throwExceptionInvalidRating(){
       ratings.add(new RatingRecord(-1,0,0,null,-1,"",0,-1));
       identifier.identifyRatingQuality(ratings,params, maxQuality, minQuality);

    }


    @Test
    public void optimalRatingEqualsMostChosenRating(){
        ratings.add(new RatingRecord(1,0,0,null,0,"",0,0));
        ratings.add(new RatingRecord(1,0,0,null,9,"",0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,"",0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,"",0,0));
        ratings.add(new RatingRecord(1,0,0,null,3,"",0,0));
        RatingRecord ratingRecord = new RatingRecord(1,0,0,null,3,"",0,0);
        ratings.add(ratingRecord);


        Map<RatingRecord,Integer> map = identifier.identifyRatingQuality(ratings,params,maxQuality, minQuality);

        assertEquals((int)map.get(ratingRecord),maxQuality);
    }


    @Test
    public void optimalRatingEqualsAverageOfMostChosenRatings(){
        RatingRecord r1a = new RatingRecord(1,0,0,null,0,"",0,0);
        RatingRecord r1b = new RatingRecord(1,0,0,null,0,"",0,0);

        RatingRecord r2a = new RatingRecord(1,0,0,null,8,"",0,0);
        RatingRecord r2b = new RatingRecord(1,0,0,null,8,"",0,0);

        RatingRecord r3 = new RatingRecord(1,0,0,null,4,"",0,0);


        ratings.add(r1a);
        ratings.add(r1b);
        ratings.add(r2a);
        ratings.add(r2b);
        ratings.add(r3);

        Map<RatingRecord,Integer> map = identifier.identifyRatingQuality(ratings,params,maxQuality, minQuality);


        assertEquals((int)map.get(r1a),5);

        assertEquals((int)map.get(r2a),5);

        assertEquals((int)map.get(r3),maxQuality);




    }

    @Test
    public void testRndRatings(){
        Random rand = new Random();
        for (int i = 0; i < rand.nextInt(50); i++) {
            ratings.add( new RatingRecord(1,0,0,null,rand.nextInt(9),"",0,0));

        }


        int optimalRating = 0;

        Map<Integer, List<RatingRecord>> sortedMap = ratings.stream().collect(Collectors.groupingBy(RatingRecord::getRating));
        ArrayList<Map.Entry<Integer, List<RatingRecord>>> ratingsSortedAfterSize = new ArrayList<>(sortedMap.entrySet());
        Collections.sort(ratingsSortedAfterSize, (o1, o2) -> Integer.compare(o1.getValue().size(), o2.getValue().size()));

        int numberOfOptRatings = 0;
        for (int i = 0; i < ratingsSortedAfterSize.size(); i++) {
            if (ratingsSortedAfterSize.get(i).getValue().size() == ratingsSortedAfterSize.get(ratingsSortedAfterSize.size() - 1).getValue().size()) {
                optimalRating += ratingsSortedAfterSize.get(i).getKey();
                numberOfOptRatings++;
            }
        }

        optimalRating = (int) Math.round(optimalRating / (double) numberOfOptRatings);


        for (Map.Entry<RatingRecord,Integer> entry: identifier.identifyRatingQuality(ratings,params,maxQuality,minQuality).entrySet()  ) {
            assertEquals((int)entry.getValue(), (maxQuality- (Math.abs(entry.getKey().getRating() - optimalRating))) );
        }

    }


    @Test (expected = IllegalArgumentException.class)
    public void throwExceptionMissingRatings(){
        identifier.identifyRatingQuality(ratings,params,maxQuality, minQuality);

    }
}