package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;

/**
 * Tuple which gives a WorkerRecord a amount to pay
 */
public class PaymentJob {
    private final WorkerRecord workerRecord;
    private final int amount;

    /**
     * Creates a new tuple for workerRecord and a amount of money
     * @param workerRecord worker to pay
     * @param amount amount to pay
     */
    public PaymentJob(WorkerRecord workerRecord, int amount) {
        this.workerRecord = workerRecord;
        this.amount = amount;
    }

    /**
     * Get the worker
     * @return WorkerRecord
     */
    public WorkerRecord getWorkerRecord() {
        return workerRecord;
    }

    /**
     * Get the amount to pay
     * @return
     */
    public int getAmount() {
        return amount;
    }
}
