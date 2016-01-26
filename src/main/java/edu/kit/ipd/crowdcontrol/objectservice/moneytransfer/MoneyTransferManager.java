package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PaymentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WokerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;

import java.io.UnsupportedEncodingException;
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

    int payOffThreshold;
    int minGiftCodesCount;
    String notificationMailAddress;
    String notificationTextHTML;


    public MoneyTransferManager(MailHandler mailHandler, PaymentOperations paymentOperations, WokerOperations workerOperations, String notificationMailAddress) throws MessagingException {
        this.mailHandler = mailHandler;
        this.wokerOperations = workerOperations;
        this.paymentOperations = paymentOperations;
        this.minGiftCodesCount = 10;
        this.payOffThreshold = 0;
        this.notificationMailAddress = notificationMailAddress;
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
        List<WorkerRecord> workers = wokerOperations.getWorkersWithCreditBalanceGreaterThan(payOffThreshold);
        Iterator<WorkerRecord> workerIt = workers.iterator();
        List<GiftCodeRecord> giftCodes = paymentOperations.getUnusedGiftCodesDescending();

        while (workerIt.hasNext()) {
            WorkerRecord worker = workerIt.next();
            List<GiftCodeRecord> payedCodesForWorker = chooseGiftCodes(worker, giftCodes);

            giftCodes = paymentOperations.getUnusedGiftCodesDescending();
            payWorker(worker, payedCodesForWorker);
        }
        if (giftCodes.size() < minGiftCodesCount) {
            notificationTextHTML = notificationTextHTML + "There are less than " + minGiftCodesCount + " giftcodes in the database. It is recommended to add more.<br/>";
        }
        sendNotification();
    }

    private List<GiftCodeRecord> chooseGiftCodes(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        List<GiftCodeRecord> payedCodes = new LinkedList<GiftCodeRecord>();
        int creditBalance = worker.getCreditBalance();
        Iterator<GiftCodeRecord> giftCodesIt = giftCodes.iterator();
        while (giftCodesIt.hasNext()) {
            if (creditBalance == 0) {
                break;
            }
            GiftCodeRecord nextCode = giftCodesIt.next();
            if (nextCode.getAmount() <= creditBalance) {
                creditBalance -= nextCode.getAmount();
                paymentOperations.markGiftCodeAsUsed(nextCode, worker);
                payedCodes.add(nextCode);
            }
        }
        if (!giftCodesIt.hasNext() && creditBalance >= 15) {
            notificationTextHTML = notificationTextHTML + "A worker has pending Payments in the amount of " + creditBalance + "ct. Please add giftcodes, so the payment of the worker can be continued.<br/>";
        }
        worker.setCreditBalance(creditBalance);
        return payedCodes;
    }

    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        if (!giftCodes.isEmpty()) {
            String message = "Dear Worker, <br/>We thank you for your work and send you in this mail the the Amazon giftcodes you earned. " +
                    "You can redeem them <a href=\"https://www.amazon.de/gc/redeem/ref=gc_redeem_new_exp\">here!</a>" +
                    "Please note, that the amount of the giftcodes can be under the amount of money you earned. " +
                    "The giftcodes with corresponding amount of money first have to be bought, or if the amount of money missing is below 15ct, you have to complete more tasks to get the complete amount of money.<br/>";
            Iterator<GiftCodeRecord> it = giftCodes.iterator();
            while (it.hasNext()) {
                message = message + it.next().getCode() + "</br>";
            }
            try {
                mailHandler.sendMail(worker.getEmail(), "Your payment for your Crowdworking", message);
            } catch (UnsupportedEncodingException | MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification() {
        try {
            mailHandler.sendMail(notificationMailAddress, "Payment Notification", notificationTextHTML);
        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }
    }
}