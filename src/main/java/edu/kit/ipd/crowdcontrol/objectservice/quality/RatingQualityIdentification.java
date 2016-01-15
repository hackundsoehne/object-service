package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lucaskrauss
 *
 * RatingQualityIdentification is class for the identification of the quality of a set of ratings
 * It uses the template-pattern to provide extendability.
 */
public abstract class RatingQualityIdentification {

    private Map<RatingRecord,Integer> qualitiesOfRatings;


    /**
     * Constructor
     */
    public RatingQualityIdentification(){
        qualitiesOfRatings = new HashMap<>();
    }



    /**
     *
     * @param records
     * @return
     * @throws IllegalArgumentException
     */
    public Map<RatingRecord, Integer> rateRatings(List<RatingRecord> records) throws IllegalArgumentException{
        concreteQuality(records);
        return qualitiesOfRatings;


    }

    abstract void concreteQuality(List<RatingRecord> records);
}
