package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PaymentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WokerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;

/**
 * Describes a Manager for money transfers. The Manager can log payments and pay off.
 *
 * @author Felix Rittler
 */
public class MoneyTransferManager {

    MailHandler mailHandler;
    PaymentOperations paymentOperations;
    WokerOperations wokerOperations;

    public MoneyTransferManager(MailHandler mailHandler, PaymentOperations paymentOperations, WokerOperations wokerOperations) throws MessagingException {
        this.mailHandler = mailHandler;
        this.wokerOperations = wokerOperations;
        this.paymentOperations = paymentOperations;
    }

    /**
     * Logs a new money transfer and saves it.
     *
     * @param workerID the id of the worker, who gets the money
     * @param amount   the amount of money in ct
     */
    public void logMoneyTransfer(int workerID, int amount) {
        WorkerRecord worker = wokerOperations.getWorker(workerID);
        worker.setCreditBalance(worker.getCreditBalance() + amount);
    }

    /**
     * Pays all workers depending on their logged money transfers.
     */
    public void payOff() {
        List<WorkerRecord> workers = wokerOperations.getAllWorkers();
        Iterator<WorkerRecord> workerIt = workers.iterator();
        List<GiftCodeRecord> giftCodes = paymentOperations.getAllGiftCodesDescending();

        while (workerIt.hasNext()) {
            WorkerRecord worker = workerIt.next();
            List<GiftCode> payedCodesForWorker = chooseGiftCodes(worker, giftCodes);
            if (payedCodesForWorker.isEmpty()) {
                //TODO : Send notification
            }
            giftCodes.removeAll(payedCodesForWorker);
            payWorker(worker, payedCodesForWorker);

        }
        if (giftCodes.size() < 10) {
            //TODO : Send notification
        }
    }

    private List<GiftCode> chooseGiftCodes(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        List<GiftCode> payedCodes = new LinkedList<GiftCode>();
        int creditBalance = worker.getCreditBalance();
        Iterator<GiftCode> payedCodesIt = payedCodes.iterator();
        Iterator<GiftCodeRecord> giftCodesIt = giftCodes.iterator();
        while (giftCodesIt.hasNext()) {
            if (creditBalance == 0) {
                break;
            }
            GiftCodeRecord nextCode = giftCodesIt.next();
            if (nextCode.getAmount() <= creditBalance) {
                creditBalance -= nextCode.getAmount();
                payedCodes.add(new GiftCode(nextCode.getCode(), nextCode.getAmount()));
            }
        }
        if (!giftCodesIt.hasNext() && creditBalance >= 15) {
            //TODO : Send Notification
        }
        worker.setCreditBalance(creditBalance);
        return payedCodes;
    }

    private void payWorker(WorkerRecord worker, List<GiftCode> giftCodes) {
        //TODO
    }

    private void sendNotification() {
        //TODO : Idea : Add all notifications to one mail and send all Problems togehter after payOff()
    }
}
