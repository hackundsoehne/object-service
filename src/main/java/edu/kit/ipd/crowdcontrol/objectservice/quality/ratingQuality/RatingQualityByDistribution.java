package edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lucaskrauss at 19.01.2016
 * Identifies the quality of a list of ratings. The ratings' qualities depend on weather they meet the most chosen rating-value or not.
 * If they do, their quality will be set to maximumQuality, else to lesser quality-values, depending on the difference of their
 * rating-value to to most-chosen rating.
 */
public class RatingQualityByDistribution implements RatingQualityStrategy {

    private final String algorithmName = "RatingQualityByDistribution";
    private final String algorithmDescription = "Identifies the quality of ratings depending on their distribution." +
            "\nRatings with the most-chosen value will be assigned the maximum-quality. Others have a quality depending on" +
            "the deviation of their value to the most chosen one";

    /**
     * Identifies the quality of a list of ratings. The ratings' qualities depend on weather they meet the most chosen rating-value or not.
     * If they do, their quality will be set to maximumQuality, else to lesser quality-values, depending on the difference of their
     * rating-value to to most-chosen rating.
     * @param ratings        list of RatingRecords whose quality will be rated
     * @param maximumQuality of the experiment
     * @param minimumQuality of the experiment
     * @return Mapping of ratings and their corresponding quality
     * @throws IllegalArgumentException
     */
    @Override
    public Map<RatingRecord, Integer> identifyRatingQuality(List<RatingRecord> ratings, int maximumQuality, int minimumQuality) throws IllegalArgumentException {

        ratings.forEach((rating) -> {
            if (rating.getRating() < 0 || rating.getRating() > 9) {
                throw new IllegalArgumentException("Error! Illegal rating value in " + this.getClass() + "! Expected value from 0 to 9"
                        + ", but got :" + rating.getRating());
            }
        });
        Map<RatingRecord, Integer> map = new HashMap<>();

        // Bucket sorts given ratings
        Map<Integer, List<RatingRecord>> sortedMap = ratings.stream().collect(Collectors.groupingBy(RatingRecord::getRating));
        int optimalRating = getOptimalRating(sortedMap);

        for (Map.Entry<Integer, List<RatingRecord>> entry : sortedMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (RatingRecord ratingRecord : entry.getValue()) {
                    int diffFromOptimal = Math.abs(ratingRecord.getRating() - optimalRating);
                    int rating = maximumQuality - diffFromOptimal;
                    map.put(ratingRecord, rating);
                }
            }
        }
        return map;
    }

    @Override
    public String getAlgorithmName() {
        return algorithmName;
    }

    @Override
    public String getAlgorithmDescription() {
        return algorithmDescription;
    }

    /**
     * Calculates optimal rating based on how much a rating-value has been chosen for an answer.
     *
     * @param sortedRatings mapping of ratings to their rating-records
     * @return the most chosen rating-values.  OR if more than one rating-value are the most chosen ones,
     * the average of those rating-values is returned.
     */
    private int getOptimalRating(Map<Integer, List<RatingRecord>> sortedRatings) {

        if (sortedRatings.size() == 0) {
            throw new IllegalArgumentException("Error! Answer without valid ratings. In " + this.getClass());
        }

        ArrayList<Map.Entry<Integer, List<RatingRecord>>> list = new ArrayList<>(sortedRatings.entrySet());
        Collections.sort(list, (o1, o2) -> Integer.compare(o1.getValue().size(), o2.getValue().size()));

        int optimalRating = 0;
        int divisor = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getValue().size() == list.get(list.size() - 1).getValue().size()) {
                optimalRating += list.get(i).getKey();
                divisor++;
            }
        }

        optimalRating = (int) Math.round(optimalRating / (double) divisor);
        return optimalRating;
    }
}

