package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

/**
 * Transforms worker protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class WorkerTransform {
    /**
     * Converts a worker record to its protobuf representation.
     *
     * @param record worker record
     *
     * @return Worker.
     */
    public static Worker toProto(WorkerRecord record) {
        return Worker.newBuilder()
                .setId(record.getIdWorker())
                .setPlatform(record.getPlatform())
                .setEmail(record.getEmail())
                .build();
    }

    /**
     * Merges a record with the set properties of a protobuf worker.
     *
     * @param target record to merge into
     * @param worker message to merge from
     *
     * @return Merged worker record.
     */
    public static WorkerRecord mergeRecord(WorkerRecord target, Worker worker) {
        if (worker.hasField(worker.getDescriptorForType().findFieldByNumber(Worker.PLATFORM_FIELD_NUMBER))) {
            target.setPlatform(worker.getPlatform());
        }

        if (worker.hasField(worker.getDescriptorForType().findFieldByNumber(Worker.EMAIL_FIELD_NUMBER))) {
            target.setEmail(worker.getEmail());
        }

        return target;
    }
}
