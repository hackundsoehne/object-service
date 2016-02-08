package edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AlgorithmAnswerQualityParamRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;

import java.util.List;
import java.util.Map;

/**
 * Created by lucaskrauss on 19.01.2016.
 * <p>
 * The AnswerQualityIdentification-interface is part of a strategy-design-pattern.
 * Classes which implement this interface calculate the quality of an answer.
 * They are passed the answer itself and additionally the answer's ratings.
 */
public interface AnswerQualityStrategy {

     final String QUALITY = "Quality";
    final String NUM_OF_RATINGS = "NumOfRatings";


    /**
     * Identifies the quality of an answer.
     *
     * @param answer           answerRecord whose quality is to be estimated
     * @param ops              db-operations object, enables the algorithm to communicate with the db
     * @param passedParameters Mapping of required parameters and their actual values specified by the operator
     * @param maximumQuality   value for an answer
     * @param minimumQuality   value for an answer
     * @return the quality of the answer mapped to the "Quality"-string,
     * and the number of ratings used to estimate its quality mapped to the "NumOfRatings"-String
     */
    Map<String,Integer> identifyAnswerQuality(AnswerRatingOperations ops, AnswerRecord answer, Map<AlgorithmAnswerQualityParamRecord, String> passedParameters, int maximumQuality, int minimumQuality) throws IllegalArgumentException;

    /**
     * Get the name of the implementing algorithm
     *
     * @return name of the algorithm
     */
    String getAlgorithmName();

    /**
     * Gets the description of the implementing algorithm
     *
     * @return a description of the algorithm
     */
    String getAlgorithmDescription();

    /**
     * Gets a list specifying the required parameters for the algorithm
     * If the algorithm doesn't require any parameters an empty list has to be returned
     *
     * @return list of required parameters or an empty list
     */
    List<AlgorithmAnswerQualityParamRecord> getParams();
}
