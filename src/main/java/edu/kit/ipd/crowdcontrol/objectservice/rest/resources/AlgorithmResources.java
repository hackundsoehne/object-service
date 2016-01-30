package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AlgorithmOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmList;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * Handles requests to algorithms resources.
 *
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmResources {
    private final AlgorithmOperations algorithmOperations;

    public AlgorithmResources(AlgorithmOperations algorithmOperations) {
        this.algorithmOperations = algorithmOperations;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return the algorithm-list
     */
    public AlgorithmList getAllAlgortihms(Request request, Response response) {
        List<AlgorithmOption> taskChoosers = algorithmOperations.getAllTaskChoosers();
        List<AlgorithmOption> answerQualityAlgorithms = algorithmOperations.getAllAnswerQualityAlgorithms();
        List<AlgorithmOption> ratingQualityAlgorithms = algorithmOperations.getAllRatingQualityAlgorithms();
        return AlgorithmList.newBuilder()
                .addAllTaskChooserAlgorithms(taskChoosers)
                .addAllAnswerQualityAlgorithms(answerQualityAlgorithms)
                .addAllRatingQualityAlgorithms(ratingQualityAlgorithms)
                .build();
    }
}
