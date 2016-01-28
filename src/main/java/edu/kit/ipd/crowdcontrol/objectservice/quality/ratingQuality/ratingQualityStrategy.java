package edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.List;
import java.util.Map;

/**
 * Created by lucaskrauss at 19.01.2016
 * <p>
 * The RatingQualityStrategy-interface is part of a strategy-design-pattern.
 * Classes which implement this interface calculate the quality of a set of ratings.
 * These classes are passed a set of ratings of an answer.
 */
public interface RatingQualityStrategy {

    /**
     * Identifies the quality of a set of ratings
     *
     * @param ratings list of RatingRecords whose quality will be rated
     * @return Map of RatingRecords and their assigned quality-value
     * @throws IllegalArgumentException If ratings have a illegal value
     */
    Map<RatingRecord, Integer> identifyRatingQuality(List<RatingRecord> ratings, int maximumQuality, int minimumQuality) throws IllegalArgumentException;
}
