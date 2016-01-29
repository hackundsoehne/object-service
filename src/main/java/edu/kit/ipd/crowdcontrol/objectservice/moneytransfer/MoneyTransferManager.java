package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.jooq.Result;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Describes a Manager for money transfers. The Manager can log payments and pay off.
 *
 * @author Felix Rittler
 */
public class MoneyTransferManager {

    MailHandler mailHandler;
    WorkerBalanceOperations workerBalanceOperations;
    WorkerOperations workerOperations;

    int payOffThreshold;
    int minGiftCodesCount;
    String notificationMailAddress;
    String notificationTextHTML;


    public MoneyTransferManager(MailHandler mailHandler, WorkerBalanceOperations workerBalanceOperations, WorkerOperations workerOperations, String notificationMailAddress) throws MessagingException {
        this.mailHandler = mailHandler;
        this.workerOperations = workerOperations;
        this.workerBalanceOperations = workerBalanceOperations;
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
    public void logMoneyTransfer(int workerID, int amount, int expID) {
        workerBalanceOperations.addCredit(workerID, amount, expID);
    }

    /**
     * Pays all workers depending on their logged money transfers.
     */
    public void payOff() {
        fetchNewGiftCodes();
        //Choose giftcodes and pay workers
        Result<WorkerRecord> workers = workerOperations.getWorkerWithCreditBalanceGreaterOrEqual(payOffThreshold);
        Iterator<WorkerRecord> workerIt = workers.iterator();
        List<GiftCodeRecord> giftCodes = workerBalanceOperations.getUnusedGiftCodes();

        while (workerIt.hasNext()) {
            WorkerRecord worker = workerIt.next();
            List<GiftCodeRecord> payedCodesForWorker = chooseGiftCodes(worker, giftCodes);

            giftCodes = workerBalanceOperations.getUnusedGiftCodes();
            payWorker(worker, payedCodesForWorker);
        }
        if (giftCodes.size() < minGiftCodesCount) {
            notificationTextHTML = notificationTextHTML + "There are less than " + minGiftCodesCount + " giftcodes in the database. It is recommended to add more.<br/>";
        }
        //Send notification to scientist
        sendNotification();
    }

    private void fetchNewGiftCodes() {
        Message[] messages = new Message[0];
        try {
            messages = mailHandler.fetchUnseen("inbox");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        for (Message message : messages) {
            try {
                GiftCodeRecord rec = MailParser.parseAmazonGiftCode(message);
                workerBalanceOperations.addGiftCode(rec.getCode(), rec.getAmount());
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            } catch (AmazonMailFormatChangedException e) {
                try {
                    mailHandler.markAsUnseen(message);
                } catch (MessagingException f) {
                    f.printStackTrace();
                }
                notificationTextHTML = notificationTextHTML + "It seems, that amazon changed the format of the giftcode E-Mails. You need to adjust the parser to import giftcodes in future.";
            }
        }
    }

    private List<GiftCodeRecord> chooseGiftCodes(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        List<GiftCodeRecord> payedCodes = new LinkedList<>();
        int creditBalance = workerBalanceOperations.getBalance(worker.getIdWorker());
        Iterator<GiftCodeRecord> giftCodesIt = giftCodes.iterator();
        while (giftCodesIt.hasNext()) {
            if (creditBalance == 0) {
                break;
            }
            GiftCodeRecord nextCode = giftCodesIt.next();
            if (nextCode.getAmount() <= creditBalance) {
                //workerBalanceOperations.addDebit(worker.getIdWorker(), nextCode.getAmount(), nextCode.getCode());
                payedCodes.add(nextCode);
            }
        }
        if (!giftCodesIt.hasNext() && creditBalance >= 15) {
            notificationTextHTML = notificationTextHTML + "A worker has pending Payments in the amount of " + creditBalance + "ct. Please add giftcodes, so the payment of the worker can be continued.<br/>";
        }
        return payedCodes;
    }

    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        if (!giftCodes.isEmpty()) {
            String message = "Dear Worker, <br/>We thank you for your work and send you in this mail the the Amazon giftcodes you earned. " +
                    "You can redeem them <a href=\"https://www.amazon.de/gc/redeem/ref=gc_redeem_new_exp\">here!</a>" +
                    "Please note, that the amount of the giftcodes can be under the amount of money you earned. " +
                    "The giftcodes with corresponding amount of money first have to be bought, or if the amount of money missing is below 15ct, you have to complete more tasks to get the complete amount of money.<br/>" +
                    "Your Giftcodes:</br>";
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