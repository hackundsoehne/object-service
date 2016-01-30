package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.WorkerBalanceType;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerBalanceRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.GIFT_CODE;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.WORKER_BALANCE;

/**
 * contains all the Operations concerned with paying the workers.
 * @author LeanderK
 * @version 1.0
 */
public class WorkerBalanceOperations extends AbstractOperations {
    protected WorkerBalanceOperations(DSLContext create) {
        super(create);
    }

    /**
     * returns all the unused GiftCodes in the system
     * @return a list with all the gift-codes
     */
    public List<GiftCodeRecord> getUnusedGiftCodes() {
        return create.selectFrom(GIFT_CODE)
                .where(GIFT_CODE.ID_GIFT_CODE.notIn(
                        DSL.select(WORKER_BALANCE.GIFT_CODE).from(WORKER_BALANCE))
                )
                .orderBy(GIFT_CODE.AMOUNT.desc())
                .fetch();
    }

    /**
     * persists the giftCode in the database
     * @param code the code of the giftCode
     * @param amount the amount its worth
     * @return true if successful
     */
    public boolean addGiftCode(String code, int amount) {
        GiftCodeRecord giftCodeRecord = create.newRecord(GIFT_CODE);
        giftCodeRecord.setCode(code);
        giftCodeRecord.setAmount(amount);
        return giftCodeRecord.store() == 1;
    }

    /**
     * adds a credit to the balance-sheet of the workers wages.
     * @param workerID the primary key of the worker the credit belongs to
     * @param amount the amount the worker has earned
     * @param experiment the experiment the worker worked on
     * @return true of successful
     */
    public boolean addCredit(int workerID, int amount, int experiment) {
        if (amount < 0)
            throw new IllegalArgumentException("amount: " + amount + " has do be positive or zero");
        WorkerBalanceRecord workerBalanceRecord = create.newRecord(WORKER_BALANCE);
        workerBalanceRecord.setExperiment(experiment);
        workerBalanceRecord.setTransactionValue(amount);
        workerBalanceRecord.setWorker(workerID);
        workerBalanceRecord.setType(WorkerBalanceType.credit);
        return workerBalanceRecord.store() == 1;
    }

    /**
     * adds a debit to the balance-sheet of the workers wages.
     * @param workerID the primary key of the worker the debit belongs to
     * @param amount the amount that got payed
     * @param giftCode the primary key of the giftCode used
     * @return true if successful
     */
    public boolean addDebit(int workerID, int amount, int giftCode) {
        if (amount > 0)
            throw new IllegalArgumentException("amount: " + amount + " has do be negative or zero");
        WorkerBalanceRecord workerBalanceRecord = create.newRecord(WORKER_BALANCE);
        workerBalanceRecord.setTransactionValue(amount);
        workerBalanceRecord.setWorker(workerID);
        workerBalanceRecord.setGiftCode(giftCode);
        workerBalanceRecord.setType(WorkerBalanceType.debit);
        return workerBalanceRecord.store() == 1;
    }

    /**
     * gets the balance of the worker.
     * @param workerID the primary key of the worker to get the balance for
     * @return the balance
     * @throws ArithmeticException if the balance does not fit into an int
     */
    public int getBalance(int workerID) throws ArithmeticException {
        return create.select(DSL.sum(WORKER_BALANCE.TRANSACTION_VALUE))
                .from(WORKER_BALANCE)
                .where(Tables.WORKER_BALANCE.WORKER.eq(workerID))
                .fetchOne()
                .value1()
                .intValueExact();
    }


}