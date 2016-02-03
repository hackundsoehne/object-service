package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.eclipse.jetty.util.IO;
import org.jooq.Result;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Describes a Manager for money transfers. The Manager can log payments and pay off.
 *
 * @author Felix Rittler
 */
public class MoneyTransferManager {

    private MailHandler mailHandler;
    private WorkerBalanceOperations workerBalanceOperations;
    private WorkerOperations workerOperations;

    private int payOffThreshold;
    private int minGiftCodesCount;

    private String notificationMailAddress;
    private StringBuilder notificationText;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> schedule = null;


    /**
     * Creates a new instance of the MoneyTransferManager
     *
     * @param mailHandler             the mailhandler, used to fetch new giftcodes and send notification and payment messages
     * @param workerBalanceOperations the workerBalanceOperations, used to change the credit balance of a worker
     * @param workerOperations        the workerOperations, used to do operations on workers
     * @param notificationMailAddress the mail address to send notifications
     */
    public MoneyTransferManager(MailHandler mailHandler, WorkerBalanceOperations workerBalanceOperations, WorkerOperations workerOperations, String notificationMailAddress) {
        this.mailHandler = mailHandler;
        this.workerOperations = workerOperations;
        this.workerBalanceOperations = workerBalanceOperations;
        this.minGiftCodesCount = 10;
        this.payOffThreshold = 0;
        this.notificationMailAddress = notificationMailAddress;
        this.notificationText = new StringBuilder();
    }

    /**
     * Starts the MoneyTransferManager, so giftcodes become submitted to workers every 7 days.
     */
    public synchronized void start() {
        if (schedule != null) {
            throw new IllegalStateException("run() was called twice!");
        }

        //schedule = scheduler.scheduleAtFixedRate(this::submitGiftCodes, 7, 7, TimeUnit.DAYS);

    }

    /**
     * Shuts the MoneyTransferManager down.
     */
    public synchronized void shutdown() {
        schedule.cancel(false);
        scheduler.shutdown();
        schedule = null;
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
    public void submitGiftCodes() throws AmazonMailFormatChangedException, MessagingException, IOException {
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

    private void fetchNewGiftCodes() throws AmazonMailFormatChangedException, MessagingException {
        Message[] messages;

        messages = mailHandler.fetchUnseen("inbox");

        for (Message message : messages) {
            try {
                GiftCodeRecord rec = MailParser.parseAmazonGiftCode(message);
                workerBalanceOperations.addGiftCode(rec.getCode(), rec.getAmount());
            } catch (AmazonMailFormatChangedException e) {
                try {
                    mailHandler.markAsUnseen(message);
                    throw new AmazonMailFormatChangedException();
                } catch (MessagingException f) {
                    f.printStackTrace();
                }
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

    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) throws MessagingException, IOException {
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


            mailHandler.sendMail(worker.getEmail(), "Your payment for your Crowdworking", paymentMessage.toString());

        }
    }

    private void sendNotification() throws UnsupportedEncodingException, MessagingException {

        mailHandler.sendMail(notificationMailAddress, "Payment Notification", notificationText.toString());

    }

    private StringBuilder loadMessage(String path) throws IOException {
        StringBuilder content = new StringBuilder();

        FileReader file = new FileReader(path);
        BufferedReader reader = new BufferedReader(file);

        String messageLine;

        while ((messageLine = reader.readLine()) != null) {
            content.append(messageLine);
            content.append(System.getProperty("line.separator"));
        }

        return content;
    }
}