package edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lucaskrauss at 19.01.2016
 */
public class RatingQualityByDistribution implements RatingQualityStrategy {



    @Override
    public Map<RatingRecord, Integer> identifyRatingQuality(List<RatingRecord> ratings) throws IllegalArgumentException {

        Map<RatingRecord, Integer> map = new HashMap<>();

        for (RatingRecord rating : ratings){


        }



        return null;



    }
}
