package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.WorkerTransform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import edu.kit.ipd.crowdcontrol.objectservice.proto.WorkerList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Handles requests to worker resources.
 *
 * @author Niklas Keller
 */
public class WorkerResource {
    private WorkerOperations operations;
    private PlatformManager manager;

    public WorkerResource(WorkerOperations operations, PlatformManager manager) {
        this.operations = operations;
        this.manager = manager;
    }

    /**
     * @param request  request provided by Spark.
     * @param response response provided by Spark.
     *
     * @return Worker if found.
     */
    public Worker identify(Request request, Response response) {
        try {
            return manager.getWorker(request.params("platform"), request.queryMap().toMap())
                    .map(WorkerTransform::toProto)
                    .orElseThrow(() -> new NotFoundException("Resource not found."));
        } catch (UnidentifiedWorkerException e) {
            throw new BadRequestException("Unidentified worker!");
        }
    }

    /**
     * @param request  request provided by Spark.
     * @param response response provided by Spark.
     *
     * @return A list of all workers.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return operations.getWorkerList(from, asc, 20)
                .constructPaginated(WorkerList.newBuilder(), WorkerList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark.
     * @param response response provided by Spark.
     *
     * @return A single worker.
     */
    public Worker get(Request request, Response response) {
        return operations.getWorkerProto(getParamInt(request, "id"))
                .orElseThrow(() -> new NotFoundException("Resource not found."));
    }

    /**
     * @param request  request provided by Spark.
     * @param response response provided by Spark.
     *
     * @return Created worker.
     */
    public Worker put(Request request, Response response) {
        Worker worker = request.attribute("input");
        String identity;

        try {
            identity = manager.identifyWorker(request.params("platform"), request.queryMap().toMap());
        } catch (UnidentifiedWorkerException e) {
            throw new BadRequestException("Unidentified worker!");
        }

        worker = operations.createWorker(worker, identity);

        response.status(201);
        response.header("Location", "/workers/" + worker.getId());

        return worker;
    }

    /**
     * @param request  request provided by Spark.
     * @param response response provided by Spark.
     *
     * @return {@code null}.
     */
    public Worker delete(Request request, Response response) {
        boolean existed = false;

        // TODO operations.anonymizeWorker(getParamInt(request, "id")); should accept int and return boolean

        if (!existed) {
            throw new NotFoundException("Worker does not exist!");
        }

        return null;
    }
}