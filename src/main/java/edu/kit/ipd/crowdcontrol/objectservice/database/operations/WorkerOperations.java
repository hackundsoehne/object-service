package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;

/**
 * Responsible for the operations involving the worker-table.
 * @author LeanderK
 * @version 1.0
 */
public class WorkerOperations extends AbstractOperations {

    protected WorkerOperations(DSLContext create) {
        super(create);
    }

    /**
     * inserts the WorkerRecord into the database.
     * @param workerRecord the record to insert
     * @return the resulting WorkerRecord existing in the database
     */
    public WorkerRecord createWorker(WorkerRecord workerRecord) {
        workerRecord.setIdWorker(null);
        return create.transactionResult(conf -> {
            boolean existing = DSL.using(conf).fetchExists(
                    DSL.selectFrom(Tables.WORKER)
                            .where(Tables.WORKER.PLATFORM.eq(workerRecord.getPlatform()))
                            .and(Tables.WORKER.IDENTIFICATION.eq(workerRecord.getIdentification()))
            );
            if (existing) {
                throw new IllegalArgumentException("worker with the same platform and identification is" +
                        "already existing");
            }
            return create.insertInto(Tables.WORKER)
                    .set(workerRecord)
                    .returning()
                    .fetchOne();
        });
    }

    /**
     * deletes a worker.
     * @param workerRecord the record to delete, the ID must be set
     * @return true if deleted
     */
    public boolean deleteWorker(WorkerRecord workerRecord) throws IllegalArgumentException {
        assertHasPrimaryKey(workerRecord);
        return create.executeDelete(workerRecord) == 1;
    }

    /**
     * deletes the worker and assigns all his work to the anonymous worker.
     * <p>
     * The worker will be deleted, there is no way to pay him after this action.
     * @param workerRecord the worker to
     * @throws IllegalArgumentException if the primary key is not set or the worker is not existing in the database
     */
    public void anonymizeWorker(WorkerRecord workerRecord) throws IllegalArgumentException {
        assertHasPrimaryKey(workerRecord);

        WorkerRecord toAnonymize = create.selectFrom(Tables.WORKER)
                .where(Tables.WORKER.ID_WORKER.eq(workerRecord.getIdWorker()))
                .fetchOptional()
                .orElseThrow(() -> new IllegalArgumentException("worker: " + workerRecord.getIdWorker() + " is not existing"));

        WorkerRecord anonWorker = create.transactionResult(configuration ->
                DSL.using(configuration).selectFrom(Tables.WORKER)
                .where(Tables.WORKER.PLATFORM.eq(toAnonymize.getPlatform()))
                .and(Tables.WORKER.IDENTIFICATION.eq("Anonymous Worker"))
                .fetchOptional()
                .orElseGet(() ->
                        DSL.using(configuration).insertInto(Tables.WORKER)
                        .set(new WorkerRecord(null, "Anonymous Worker", toAnonymize.getPlatform(), null))
                        .returning()
                        .fetchOne()));

        create.deleteFrom(Tables.POPULATION_RESULT)
                .where(Tables.POPULATION_RESULT.WORKER.eq(toAnonymize.getIdWorker()))
                .execute();

        create.update(Tables.ANSWER)
                .set(Tables.ANSWER.WORKER_ID, anonWorker.getIdWorker())
                .where(Tables.ANSWER.WORKER_ID.eq(toAnonymize.getIdWorker()))
                .execute();

        create.update(Tables.RATING)
                .set(Tables.RATING.WORKER_ID, anonWorker.getIdWorker())
                .where(Tables.ANSWER.WORKER_ID.eq(toAnonymize.getIdWorker()))
                .execute();

        create.update(Tables.WORKER_BALANCE)
                .set(Tables.WORKER_BALANCE.WORKER, anonWorker.getIdWorker())
                .where(Tables.WORKER_BALANCE.WORKER.eq(anonWorker.getIdWorker()))
                .execute();

        create.executeDelete(toAnonymize);
    }

    /**
     * finds the worker with the passed platform and platform-identification data
     * @param platform the platform the wanted worker is working on
     * @param identification the platform-specific identification
     * @return the found worker or empty
     */
    public Optional<WorkerRecord> getWorker(String platform, String identification) {
        return create.selectFrom(Tables.WORKER)
                .where(Tables.WORKER.PLATFORM.eq(platform))
                .and(Tables.WORKER.IDENTIFICATION.eq(identification))
                .fetchOptional();
    }

    /**
     * finds the worker with the passed workerId in the database
     * @param workerID the primary-key of the worker
     * @return the found worker or empty
     */
    public Optional<WorkerRecord> getWorker(int workerID) {
        return create.selectFrom(Tables.WORKER)
                .where(Tables.WORKER.ID_WORKER.eq(workerID))
                .fetchOptional();
    }

    /**
     * returns all the workers existing in the database
     * @return a list with all the workers
     */
    public List<WorkerRecord> getAllWorkers() {
        return create.selectFrom(Tables.WORKER)
                .fetch();
    }
}
