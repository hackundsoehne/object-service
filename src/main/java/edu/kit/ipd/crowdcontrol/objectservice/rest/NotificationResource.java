package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationRestOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.NotificationList;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

public class NotificationResource {
    private NotificationRestOperations operations;

    public NotificationResource(NotificationRestOperations operations) {
        this.operations = operations;
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return A list of all templates.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return operations.all(from, asc, 20)
                .constructPaginated(NotificationList.newBuilder(), NotificationList.Builder::addAllItems)
                .orElseThrow(() -> new NotFoundException("No resources found."));
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return A single template.
     */
    public Notification get(Request request, Response response) {
        return operations.get(getParamInt(request, "id"))
                .orElseThrow(() -> new NotFoundException("Resource not found."));
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return The created template.
     */
    public Notification put(Request request, Response response) {
        Notification notification = request.attribute("input");
        notification = operations.create(notification);

        response.status(201);
        response.header("Location", "/notifications/" + notification.getId());

        return notification;
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return The modified template.
     */
    public Notification patch(Request request, Response response) {
        Notification notification = request.attribute("input");
        return operations.update(getParamInt(request, "id"), notification);
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return {@code null}.
     */
    public Notification delete(Request request, Response response) {
        boolean existed = operations.delete(getParamInt(request, "id"));

        if (!existed) {
            throw new NotFoundException("Template does not exist!");
        }

        return null;
    }
}
