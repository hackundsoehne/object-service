package edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.List;

/**
 * Created by lucaskrauss on 19.01.2016.
 *
 * Implementation of the strategy-interface AnswerQualityIdentification.
 * The quality of an answer is based on its ratings. It is equal to the rounded-down
 * average rating of all ratings of the answer.
 *
 *
 *
 */
public class AnswerQualityByRatings implements AnswerQualityStrategy {

    /**
     * Identifies the quality of an answer based on its ratings.
     * The answer's quality is equal to the average (rounded down) of all its ratings.
     *
     * @param answer answerRecord whose quality is to be estimated
     * @param ratings of the given answer
     * @throws IllegalArgumentException if rating-value isn't out of (0,9).
     * @return quality of an answer
     */
    @Override
    public int identifyAnswerQuality(AnswerRecord answer, List<RatingRecord> ratings)throws IllegalArgumentException{

        int answerQuality = -1;
        for (RatingRecord rating : ratings){
            if(rating.getRating() > 9 || rating.getRating() < 0){
                throw new IllegalArgumentException("Error! Illegal rating value in "+this.getClass()+"! Expected value from 0 to 9" +
                        ", but got :" + rating.getRating());
            }
            answerQuality += rating.getRating();
        }

        answerQuality = answerQuality/ratings.size();

        return answerQuality;
    }



}
