package edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;

import java.util.List;

/**
 * Created by lucaskrauss on 19.01.2016.
 * <p>
 * The AnswerQualityIdentification-interface is part of a strategy-design-pattern.
 * Classes which implement this interface calculate the quality of an answer.
 * They are passed the answer itself and additionally the answer's ratings.
 */
public interface AnswerQualityStrategy {

    /**
     * Identifies the quality of an answer.
     *
     * @param answer  answerRecord whose quality is to be estimated
     * @param ratings of the given answer
     * @return the quality of the answer based on the implementation
     */
    int identifyAnswerQuality(AnswerRecord answer, List<RatingRecord> ratings, int maximumQuality, int minimumQuality) throws IllegalArgumentException;
}
