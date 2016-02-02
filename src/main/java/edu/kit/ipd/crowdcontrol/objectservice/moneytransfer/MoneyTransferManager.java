package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.jooq.Result;

import java.io.*;
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
    StringBuilder notificationText;


    public MoneyTransferManager(MailHandler mailHandler, WorkerBalanceOperations workerBalanceOperations, WorkerOperations workerOperations, String notificationMailAddress) throws MessagingException {
        this.mailHandler = mailHandler;
        this.workerOperations = workerOperations;
        this.workerBalanceOperations = workerBalanceOperations;
        this.minGiftCodesCount = 10;
        this.payOffThreshold = 0;
        this.notificationMailAddress = notificationMailAddress;
        this.notificationText = new StringBuilder();
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
            notificationText.append("There are less than ").append(minGiftCodesCount).append(" giftcodes in the database. It is recommended to add more.").append(System.getProperty("line.separator"));
        }

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
                notificationText.append("It seems, that amazon changed the format of the giftcode E-Mails. You need to adjust the parser to import giftcodes in future.");
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
                payedCodes.add(nextCode);
                creditBalance -= nextCode.getAmount();
            }
        }

        if (!giftCodesIt.hasNext() && creditBalance >= 15) {
            notificationText = notificationText.append("A worker has pending Payments in the amount of ").append(creditBalance).append("ct. Please add giftcodes, so the payment of the worker can be continued.").append(System.getProperty("line.separator"));
        }
        return payedCodes;
    }

    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        if (!giftCodes.isEmpty()) {
            StringBuilder paymentMessage = loadMessage("src/main/resources/PaymentMessage.txt");
            StringBuilder giftCodeMessage = new StringBuilder();

            for (GiftCodeRecord rec : giftCodes) {
                workerBalanceOperations.addDebit(worker.getIdWorker(), rec.getAmount(), rec.getIdGiftCode());
                giftCodeMessage.append(rec.getCode()).append(System.getProperty("line.separator"));
            }

            Map<String, String> map = new HashMap<>();
            map.put("GiftCodes", giftCodeMessage.toString());
            paymentMessage = new StringBuilder(Template.apply(paymentMessage.toString(), map));

            try {
                mailHandler.sendMail(worker.getEmail(), "Your payment for your Crowdworking", paymentMessage.toString());
            } catch (UnsupportedEncodingException | MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification() {
        try {
            mailHandler.sendMail(notificationMailAddress, "Payment Notification", notificationText.toString());
        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder loadMessage(String path) {
        StringBuilder content = new StringBuilder();
        try {
            FileReader file = new FileReader(path);
            BufferedReader reader = new BufferedReader(file);

            String messageLine;

            while ((messageLine = reader.readLine()) != null) {
                content.append(messageLine);
                content.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}