package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;

/**
 * Describes a Manager for money transfers. The Manager can log payments and pay off.
 * @author Felix Rittler
 */
public class MoneyTransferManager {


    public MoneyTransferManager(){}

    /**
     * Logs a new money transfer and saves it.
     * @param workerID the id of the worker, who gets the money
     * @param amount the amount of money in ct
     */
    public void logMoneyTransfer(int workerID, int amount) {
        DatabaseManager manager = new DatabaseManager()
    }

    /**
     * Pays all workers depending on their logged money transfers.
     */
    public void payOff(){}
}
