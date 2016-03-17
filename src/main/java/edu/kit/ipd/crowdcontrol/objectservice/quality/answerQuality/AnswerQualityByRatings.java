package edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AlgorithmAnswerQualityParamRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lucaskrauss on 19.01.2016.
 * <p>
 * Implementation of the strategy-interface AnswerQualityIdentification.
 * The quality of an answer is based on its ratings. It is equal to the rounded-down
 * average rating of all ratings of the answer.
 */
public class AnswerQualityByRatings implements AnswerQualityStrategy {

    private final Logger log = LogManager.getLogger(AnswerQualityByRatings.class);
    static final String ALGORITHM_NAME = "AnswerQualityByRatings";
    static final String ALGORITHM_DESCRIPTION = "Identifies the quality of answers based on its ratings. " +
            "\nThe answer's quality is equal to the rounded average of all its ratings ";
    static final String REGEX = "[0-9]";
    static final String DB_REGEX = "^" + REGEX + "$";
    static final String PARAM_DESCRIPTION = "The algorithm's parameter specifies which ratings are considered \"good\"." +
            "All ratings with a quality equal or greater than this parameter will be considered \"good\" " +
            "Good ratings of an answer will be used to estimate the quality of the answer. This parameter has to be a positive integer between 0 and 9. ";
    static final String PARAMETER_ID = "RatingQualityThreshold";

    private int ratingQualityThreshold;

    /**
     * Identifies the quality of an answer based on its ratings.
     * The answer's quality is equal to the average (rounded down) of all its ratings.
     *
     * @param answer           answerRecord whose quality is to be estimated
     * @param ops              db-operations object, enables the algorithm to communicate with the db
     * @param passedParameters Mapping of required parameters and their actual values specified by the operator
     * @param maximumQuality   value for an answer
     * @param minimumQuality   value for an answer
     * @return quality-value of the answer
     * @throws IllegalArgumentException if rating-value isn't out of (0,9).
     */
    @Override
    public Map<String, Integer> identifyAnswerQuality(AnswerRatingOperations ops, AnswerRecord answer, Map<AlgorithmAnswerQualityParamRecord, String> passedParameters, int maximumQuality, int minimumQuality) throws IllegalArgumentException {

        Map<String, Integer> map = new HashMap<>();

        //Fetch parameter
        passedParameters.entrySet().forEach(entry -> {
            if (entry.getKey().getData().equals(PARAMETER_ID)) {
                ratingQualityThreshold = Integer.valueOf(entry.getValue());
                if (ratingQualityThreshold < 0 || ratingQualityThreshold > 9) {
                    log.fatal(String.format("Error! Received illegal argument for %s! Should: %s   Is: %s! \n Setting threshold to 5.", entry.getKey().getAlgorithm(), entry.getKey().getRegex(), entry.getValue()));
                    ratingQualityThreshold = 5;
                }
            }
        });

        //Retrieve "good" ratings
        List<RatingRecord> ratings = ops.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);
        if(ratings.size() == 0){
            map.put(QUALITY,answer.getQuality());
            map.put(NUM_OF_RATINGS,0);
            return map;
        }
        if (ratings.size() == 1 && ratings.get(0).getRating() >= 0 && ratings.get(0).getRating() < 10) {
            map.put(QUALITY, ratings.get(0).getRating());
            map.put(NUM_OF_RATINGS, 1);
            return map;
        }

        int answerQuality = 0;
        for (RatingRecord rating : ratings) {
            if (rating.getRating() > 9 || rating.getRating() < 0) {
                throw new IllegalArgumentException("Error! Illegal rating value in " + this.getClass() + "! Expected value from 0 to 9" +
                        ", but got :" + rating.getRating());
            }
            answerQuality += rating.getRating();
        }
        map.put(QUALITY, (int) Math.round(answerQuality / (double) ratings.size()));
        map.put(NUM_OF_RATINGS, ratings.size());


        return map;
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    @Override
    public String getAlgorithmDescription() {
        return ALGORITHM_DESCRIPTION;
    }

    @Override
    public List<AlgorithmAnswerQualityParamRecord> getParams() {
        return Collections.singletonList(new AlgorithmAnswerQualityParamRecord(null, PARAM_DESCRIPTION, DB_REGEX, ALGORITHM_NAME, PARAMETER_ID));
    }


}
