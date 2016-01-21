package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerBalanceRecord;
import org.jooq.DSLContext;

import java.util.List;

/**
 * contains all the Operations needed for the Payment-Table
 * @author LeanderK
 * @version 1.0
 */
public class PaymentOperations extends AbstractOperations {
    protected PaymentOperations(DSLContext create) {
        super(create);
    }

    public List<GiftCodeRecord> getAllGiftCodes() {
        //TODO
        return null;
    }

    public void addGiftCode(String code, int amount){
        //TODO
    }

    public void addTranscation(int workerID, int amount, int experiment) {
        //TODO
    }

    public void addTranscation(int workerID, int amount, int experiment, int giftCard) {
        //TODO
    }

    public int getCreditBalance() {
        //TODO
        return -1;
    }

    /**
     * Returns all the transactions for the passed workerID.
     * @param workerID the primary key of the worker
     * @return a list of transactions
     */
    public List<WorkerBalanceRecord> getAllPayments(int workerID) {
        return create.selectFrom(Tables.WORKER_BALANCE)
                .where(Tables.WORKER_BALANCE.WORKER.eq(workerID))
                .fetch();
    }


}