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


    /**
     * @param ratings        list of RatingRecords whose quality will be rated
     * @param maximumQuality
     * @param minimumQuality
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Map<RatingRecord, Integer> identifyRatingQuality(List<RatingRecord> ratings, int maximumQuality, int minimumQuality) throws IllegalArgumentException {

        Map<RatingRecord, Integer> map = new HashMap<>();

        // Bucket sorts given ratings
        LinkedList<RatingRecord>[] sortedRatings = new LinkedList[10];
        for (RatingRecord rating : ratings) {

            if (rating.getRating() < 0 || rating.getRating() > 9) {
                throw new IllegalArgumentException("Error! Illegal rating value in " + this.getClass() + "! Expected value from 0 to 9" +
                        ", but got :" + rating.getRating());
            } else if (sortedRatings[rating.getRating()] == null) {
                sortedRatings[rating.getRating()] = new LinkedList<RatingRecord>();
            }
            sortedRatings[rating.getRating()].add(rating);
        }

        int optimalRating = getOptimalRating(sortedRatings);

        for (int i = 0; i < sortedRatings.length; i++) {
            if (sortedRatings[i] != null) {
                for (RatingRecord ratingRecord : sortedRatings[i]) {

                    int diffFromOptimal = Math.abs(ratingRecord.getRating() - optimalRating);
                    int rating = maximumQuality - diffFromOptimal;
                    //TODO Median or sth
                    map.put(ratingRecord, rating);
                }
            }
        }
        return null;
    }

    /**
     * Calculates optimal rating based on how much a rating-value has been chosen for an answer.
     *
     * @param sortedRatings sorted array of lists of rating-records (product of bucket-sort)
     * @return the most chosen rating-values.  OR if more than one rating-value are the most chosen ones,
     * the average of those rating-values is returned.
     */
    private int getOptimalRating(LinkedList<RatingRecord>[] sortedRatings) {

        int optimalRating = 0;

        //Gets most chosen rating (optimal rating) depending on the lengths of the sub lists
        List<List<RatingRecord>> mostChosenRating = new LinkedList<>();

        for (int i = 0; i < sortedRatings.length; i++) {
            if (sortedRatings[i] != null && (mostChosenRating.get(0) == null || sortedRatings[i].size() > mostChosenRating.get(0).size())) {
                mostChosenRating.set(0, sortedRatings[i]);
            } else if ((sortedRatings[i] != null) && (sortedRatings[i].size() == mostChosenRating.get(0).size())) {
                mostChosenRating.add(sortedRatings[i]);
            }
        }

        if (mostChosenRating.size() == 0) {
            throw new IllegalArgumentException("Error! Answer without valid ratings. In " + this.getClass());
        } else {
            //If just one rating-value is the most chosen one, it is returned, else the average of the most
            //chosen ratings will be returned
            for (int i = 0; i < mostChosenRating.size(); i++) {
                optimalRating += mostChosenRating.get(i).get(0).getRating();
            }
            optimalRating = optimalRating / mostChosenRating.size();
        }

        return optimalRating;
    }
}

