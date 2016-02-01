package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.proto.CalibrationAnswer;
import org.jooq.DSLContext;

/**
 * Responsible for the operations involving the worker calibrations.
 *
 * @author Niklas Keller
 */
public class WorkerCalibrationOperations extends AbstractOperations {
    public WorkerCalibrationOperations(DSLContext create) {
        super(create);
    }

    /**
     * Inserts a new calibration answer.
     * @param workerId ID of the worker
     * @param answer answer of the worker
     * @return Inserted calibration answer.
     */
    public CalibrationAnswer insertAnswer(int workerId, CalibrationAnswer answer) {
        // TODO: @Leander
        return null;
    }
}
