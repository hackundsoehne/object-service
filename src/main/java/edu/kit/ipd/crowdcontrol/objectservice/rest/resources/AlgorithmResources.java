package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AlgorithmOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getQueryBool;

/**
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
     * @return List of TaskChooser-algorithms.
     */
    public Paginated<String> allTaskChoosers(Request request, Response response) {
        String from = request.queryParams("from");
        boolean asc = getQueryBool(request, "asc", true);

        return algorithmOperations.getTaskChoosersFrom(from == null ? "" : from, asc, 20)
                .constructPaginated(AlgorithmList.newBuilder(), AlgorithmList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return List of AnswerQuality-algorithms.
     */
    public Paginated<String> allAnswerQualityAlgorithms(Request request, Response response) {
        String from = request.queryParams("from");
        boolean asc = getQueryBool(request, "asc", true);

        return algorithmOperations.getAnswerQualityAlgorithmsFrom(from == null ? "" : from, asc, 20)
                .constructPaginated(AlgorithmList.newBuilder(), AlgorithmList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return List of RatingQuality-algorithms.
     */
    public Paginated<String> allRatingQualityAlgorithms(Request request, Response response) {
        String from = request.queryParams("from");
        boolean asc = getQueryBool(request, "asc", true);

        return algorithmOperations.getRatingQualityAlgorithmsFrom(from == null ? "" : from, asc, 20)
                .constructPaginated(AlgorithmList.newBuilder(), AlgorithmList.Builder::addAllItems);
    }
}
