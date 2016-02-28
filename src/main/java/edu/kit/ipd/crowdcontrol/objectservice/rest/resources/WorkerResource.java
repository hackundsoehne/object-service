package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.WorkerTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import edu.kit.ipd.crowdcontrol.objectservice.proto.WorkerList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.Objects;
import java.util.Optional;

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
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Identified worker.
     */
    public Worker identify(Request request, Response response) {
        try {
            WorkerRecord worker;
            String platform = request.params("platform");
            String identification = manager.identifyWorker(platform, request.queryMap().toMap());
            Optional<WorkerRecord> optionalRecord = manager.getWorker(platform, identification);
            if (optionalRecord.isPresent()) {
                worker = optionalRecord.get();
            } else if (!manager.getNeedemail(platform) || (request.queryParams("email") != null && !request.queryParams("email").isEmpty())){
                WorkerRecord workerRecord = new WorkerRecord(null, identification, platform, null, null);
                worker = operations.insertWorker(workerRecord);
            } else {
                throw new NotFoundException();
            }

            return WorkerTransformer.toProto(worker);
        } catch (UnidentifiedWorkerException e) {
            throw new BadRequestException("Could not identify worker.");
        }
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return List of workers.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return operations.getWorkersFrom(from, asc, 20)
                .constructPaginated(WorkerList.newBuilder(), WorkerList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark.
     * @param response response provided by Spark.
     *
     * @return Single worker.
     */
    public Worker get(Request request, Response response) {
        return operations.getWorkerProto(getParamInt(request, "id"))
                .orElseThrow(NotFoundException::new);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Created worker.
     */
    public Worker put(Request request, Response response) {
        Worker worker = request.attribute("input");
        String identity;

        try {
            identity = manager.identifyWorker(request.params("platform"), request.queryMap().toMap());
        } catch (UnidentifiedWorkerException e) {
            throw new BadRequestException("Could not identify worker.");
        }

        worker = operations.insertWorker(worker, identity);

        EventManager.WORKER_CREATE.emit(worker);

        response.status(201);
        response.header("Location", "/workers/" + worker.getId());

        return worker;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return patched worker.
     */
    public Worker patch(Request request, Response response) {
        Worker worker = request.attribute("input");
        int id = getParamInt(request, "id");

        worker = operations.updateWorker(worker, id);

        EventManager.WORKER_CHANGE.emit(worker);

        return worker;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return {@code null}.
     */
    public Worker delete(Request request, Response response) {
        int id = getParamInt(request, "id");

        try {
            Optional<Worker> worker = operations.getWorkerProto(id);
            worker.map(EventManager.WORKER_DELETE::emit);

            operations.anonymizeWorker(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }

        return null;
    }
}