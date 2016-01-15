package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.Comparator;
import java.util.List;

/**
 * Created by lucaskrauss
 * RatingQualityByDistribution provides an implementation of the RatingQualityIdentification class.
 * It serves as a template-implementation of the template pattern.
 *
 * The quality of given ratings is determined on the following pattern:
 * If a ratings is amongst the most chosen rating-values, its quality will be set to 9.
 * All other ratings will be assigned a quality-rating ( >=0 , <9 )depending on their deviation from
 * the most chosen rating-value.
 *
 *
 */
public class RatingQualityByDistribution extends RatingQualityIdentification {

    public  RatingQualityByDistribution(){
        super();
    }

    @Override
    void concreteQuality(List<RatingRecord>records) {
        int [] qualities = new int[10];
        int []indexOfMostChosenRating = new int[10];


        for (RatingRecord rating:records  ) {
            int index = rating.getRating();

            if(index > 9 ||  index < 0){
                //TODO Exception oder log?
                throw new IllegalArgumentException("Error in"+this.getClass()+"! Rating of a rating must not be greater than 9 or less than 0!");
            }
            qualities[index]++;
        }



    }
}
