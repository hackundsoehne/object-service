package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import org.jooq.DSLContext;

/**
 * responsible for the operations involving the worker-table.
 * @author LeanderK
 * @version 1.0
 */
public class WokerOperations extends AbstractOperations {

    protected WokerOperations(DSLContext create) {
        super(create);
    }

    public int createWorker(WorkerRecord workerRecord) {
        return create.insertInto(Tables.WORKER)
                .set(workerRecord)
                .returning()
                .fetchOne()
                .getIdWorker();
    }

    public boolean deleteWorker(WorkerRecord workerRecord) {
        return create.executeDelete(workerRecord) == 1;
    }

    public boolean anonymizeWorker(WorkerRecord workerRecord) {
        //TODO: default-Worker
        return true;
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
