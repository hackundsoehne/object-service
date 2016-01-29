package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.PlatformList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.getQueryBool;

/**
 * Handles requests to platform resources.
 *
 * @author Niklas Keller
 */
public class PlatformResource {
    private PlatformOperations operations;

    public PlatformResource(PlatformOperations operations) {
        this.operations = operations;
    }

    /**
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     *
     * @return List of platforms.
     */
    public Paginated<String> all(Request request, Response response) {
        String from = request.queryParams("from");
        boolean asc = getQueryBool(request, "asc", true);

        return operations.getPlatformList(from == null ? "" : from, asc, 20)
                .constructPaginated(PlatformList.newBuilder(), PlatformList.Builder::addAllItems);
    }

    /**
     * @param request  Request provided by Spark.
     * @param response Response provided by Spark.
     *
     * @return Single platform.
     */
    public Platform get(Request request, Response response) {
        return operations.getPlatform(request.params("id"))
                .orElseThrow(NotFoundException::new);
    }
}
