package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.NotificationList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Handles requests to notification resources.
 *
 * @author Niklas Keller
 */
public class NotificationResource {
    private NotificationOperations operations;

    public NotificationResource(NotificationOperations operations) {
        this.operations = operations;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return List of notifications.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return operations.getNotificationsFrom(from, asc, 20)
                .constructPaginated(NotificationList.newBuilder(), NotificationList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Single notification.
     */
    public Notification get(Request request, Response response) {
        return operations.getNotification(getParamInt(request, "id"))
                .orElseThrow(NotFoundException::new);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Created notification.
     */
    public Notification put(Request request, Response response) {
        Notification notification = request.attribute("input");

        try {
            notification = operations.insertNotification(notification);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Missing at least one required parameter.");
        }

        EventManager.NOTIFICATION_CREATE.emit(notification);

        response.status(201);
        response.header("Location", "/notifications/" + notification.getId());

        return notification;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Modified notification.
     */
    public Notification patch(Request request, Response response) {
        int id = getParamInt(request, "id");
        Notification patch = request.attribute("input");

        Notification oldNotification = operations.getNotification(id).orElseThrow(NotFoundException::new);
        Notification newNotification = operations.updateNotification(id, patch);

        EventManager.NOTIFICATION_UPDATE.emit(new ChangeEvent<>(oldNotification, newNotification));

        return newNotification;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return {@code null}.
     */
    public Notification delete(Request request, Response response) {
        int id = getParamInt(request, "id");

        Optional<Notification> notification = operations.getNotification(id);
        notification.map(EventManager.NOTIFICATION_DELETE::emit);

        boolean existed = operations.deleteNotification(id);

        if (!existed) {
            throw new NotFoundException();
        }

        return null;
    }
}
