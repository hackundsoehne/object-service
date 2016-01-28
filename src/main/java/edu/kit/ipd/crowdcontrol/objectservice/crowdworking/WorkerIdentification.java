package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;

import java.util.Map;
import java.util.Optional;

/**
 * Created by marcel on 19.01.16.
 */
@FunctionalInterface
public interface WorkerIdentification {
    /**
     * Parse a worker id out of the params
     *
     * @param param The parameters which were sent by a platform
     * @return The id of the worker if one can be found
     */
    String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException;

    /**
     * Get a worker record from the identification
     *
     * @param workerOperations The worker operations to use
     * @param platform         The platform on which this is called
     * @param param            The parameters of the platform
     * @return A WorkerRecord, if one is found
     * @throws UnidentifiedWorkerException
     */
    default Optional<WorkerRecord> getWorker(WorkerOperations workerOperations, String platform, Map<String, String[]> param) throws UnidentifiedWorkerException {
        String uid = identifyWorker(param);
        return workerOperations.getWorker(platform, uid);
    }
}
