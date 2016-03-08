package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;

/**
 * Tuple which gives a WorkerRecord a amount to pay
 */
public class PaymentJob {
    private final WorkerRecord workerRecord;
    private final int amount;
    private final String message;
    /**
     * Creates a new tuple for workerRecord and a amount of money
     * @param workerRecord worker to pay
     * @param amount amount to pay
     * @param message the message to pass to the worker which should get payed.
     */
    public PaymentJob(WorkerRecord workerRecord, int amount, String message) {
        this.workerRecord = workerRecord;
        this.amount = amount;
        this.message = message;
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

    /**
     * Get the message which should be passed to the worker
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
