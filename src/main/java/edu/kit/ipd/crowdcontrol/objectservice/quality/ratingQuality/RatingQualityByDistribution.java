package edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lucaskrauss at 19.01.2016
 */
public class RatingQualityByDistribution implements RatingQualityStrategy {




    @Override
    public Map<RatingRecord, Integer> identifyRatingQuality(List<RatingRecord> ratings) throws IllegalArgumentException {

        Map<RatingRecord, Integer> map = new HashMap<>();

        //Count the number of ratings for all possible rating-values
        List<List<RatingRecord>> listOfRatings = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            listOfRatings.add(new LinkedList<RatingRecord>());
        }

        for (RatingRecord rating : ratings){
            if(rating.getRating() < 0 || rating.getRating() >9){
                throw new IllegalArgumentException("Error! Illegal rating value in "+this.getClass()+"! Expected value from 0 to 9" +
                        ", but got :" + rating.getRating());
            }
            listOfRatings.get(rating.getRating()).add(rating);
        }

        List<List<RatingRecord>> mostChoosenRating = new LinkedList<>();


        for (int i = 0; i < 10 ; i++) {
            if (mostChoosenRating.get(0) == null || listOfRatings.get(i).size() > mostChoosenRating.get(0).size()){
                mostChoosenRating.set(0,listOfRatings.get(i));
            }else if(listOfRatings.get(i).size() == mostChoosenRating.get(0).size()){
                mostChoosenRating.add(listOfRatings.get(i));
            }

        }


        int optimalRating;

        if(mostChoosenRating.size() == 0) {
            throw new IllegalArgumentException("Error! Answer without valid ratings. In " + this.getClass());
        }else if (mostChoosenRating.size() == 0){
            optimalRating = mostChoosenRating.get(0).get(0).getRating();

        }else{
            //TODO need clarification -> Böhm/Anika

            optimalRating=0;
        }



        return null;



    }
}

