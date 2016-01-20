package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Responsible for the operations involving the worker-table.
 * @author LeanderK
 * @version 1.0
 */
public class WokerOperations extends AbstractOperations {

    protected WokerOperations(DSLContext create) {
        super(create);
    }

    /**
     * inserts the WorkerRecord into the database.
     * @param workerRecord the record to insert
     * @return the resulting WorkerRecord existing in the database
     */
    public WorkerRecord createWorker(WorkerRecord workerRecord) {
        workerRecord.setIdWorker(null);
        return create.insertInto(Tables.WORKER)
                .set(workerRecord)
                .returning()
                .fetchOne();
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

        create.update(Tables.PAYMENT)
                .set(Tables.PAYMENT.WORKER_ID, anonWorker.getIdWorker())
                .where(Tables.PAYMENT.WORKER_ID.eq(anonWorker.getIdWorker()))
                .execute();

        create.executeDelete(toAnonymize);
    }

    //either implement this:
    public AnswerRecord getCreditBalance(int workerID) { //change to CreditBalanceRecord if implemented
        //TODO
        return null;
    }

    //or implement this and implement a "public java.lang.Integer getCreditBalance()"-Method into WorkerRecord (Felix)
    public WorkerRecord getAllWorkers() {
        //TODO
        return null;
    }
}
