package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import java.util.Map;
import java.util.Optional;

/**
 * this interface is used to identify workers
 * @author LeanderK
 * @version 1.0
 */
@FunctionalInterface
public interface WorkerIdentificationComputation {
    /**
     * if the Platform has his own worker identification the interface can be returned here.
     * if passed null it should return something if getWorker is supported.
     * @param params The parameters which were sent by a platform
     * @return the value to indicate if it supports worker identification or not.
     * @throws UnidentifiedWorkerException if passed invalid params
     */
    WorkerIdentification getWorker(Map<String, String[]> params) throws UnidentifiedWorkerException;
}
