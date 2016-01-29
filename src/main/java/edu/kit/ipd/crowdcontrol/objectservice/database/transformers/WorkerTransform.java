package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

/**
 * Transforms worker protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class WorkerTransform extends AbstractTransform {
    /**
     * Converts a worker record to its protobuf representation.
     *
     * @param record worker record
     *
     * @return Worker.
     */
    public static Worker toProto(WorkerRecord record) {
        return builder(Worker.newBuilder())
                .set(record.getEmail(), Worker.Builder::setEmail)
                .getBuilder()
                .setId(record.getIdWorker())
                .setPlatform(record.getPlatform())
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
        return merge(target, worker, (field, record) -> {
            switch (field) {
                case Worker.PLATFORM_FIELD_NUMBER: record.setPlatform(worker.getPlatform());
                    break;
                case Worker.EMAIL_FIELD_NUMBER: record.setEmail(worker.getEmail());
                    break;
            }
        });
    }
}
