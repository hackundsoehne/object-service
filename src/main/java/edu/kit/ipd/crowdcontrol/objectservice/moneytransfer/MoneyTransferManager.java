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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getRootLogger();


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

        Runnable runnable = () -> {
            try {
                submitGiftCodes();
            } catch (MoneyTransferException e) {
                sendErrorMessage(e.toString());
                LOGGER.error(e);
            }
        };


        schedule = scheduler.scheduleAtFixedRate(runnable, 7, 7, TimeUnit.DAYS);

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
    public void addMoneyTransfer(int workerID, int amount, int expID) {
        workerBalanceOperations.addCredit(workerID, amount, expID);
    }

    /**
     * Pays all workers depending on their logged money transfers.
     */
    public void submitGiftCodes() throws MoneyTransferException {
        LOGGER.trace("Started fetching new giftcodes.");
        fetchNewGiftCodes();
        LOGGER.trace("Completed fetching new giftcodes.");

        LOGGER.trace("Started submission of giftcodes to workers.");
        Result<WorkerRecord> workers = workerOperations.getWorkerWithCreditBalanceGreaterOrEqual(payOffThreshold);
        List<GiftCodeRecord> giftCodes = workerBalanceOperations.getUnusedGiftCodes();

        for (WorkerRecord worker : workers) {
            List<GiftCodeRecord> payedCodesForWorker = chooseGiftCodes(worker, giftCodes);

            giftCodes = workerBalanceOperations.getUnusedGiftCodes();
            payWorker(worker, payedCodesForWorker);
        }
        if (giftCodes.size() < minGiftCodesCount) {
            notificationText.append("There are less than ").append(minGiftCodesCount).append(" giftcodes in the database. It is recommended to add more.").append(System.getProperty("line.separator"));
        }
        LOGGER.trace("Completed submission of giftcodes to workers.");

        //sends a notification about problems with submission of giftcodes
        sendNotification();
    }

    private void fetchNewGiftCodes() throws MoneyTransferException {
        Message[] messages;

        //fetch new mails
        try {
            messages = mailHandler.fetchUnseen("inbox");
        } catch (MessagingException e) {
            throw new MoneyTransferException("The MailHandler couldn't fetch new giftcodes from the mailserver. " +
                    "It seems, that there is either a problem with the server or with the properties file.");
        }

        //extract giftcodes and save them to the database
        for (Message message : messages) {
            try {
                GiftCodeRecord rec = MailParser.parseAmazonGiftCode(message);
                workerBalanceOperations.addGiftCode(rec.getCode(), rec.getAmount());
            } catch (MoneyTransferException e) {
                try {
                    mailHandler.markAsUnseen(message);
                    throw new MoneyTransferException(e.getMessage());
                } catch (MessagingException f) {
                    f.printStackTrace();
                }
            }
        }
    }

    private List<GiftCodeRecord> chooseGiftCodes(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {
        List<GiftCodeRecord> payedCodes = new ArrayList<>();
        int creditBalance = workerBalanceOperations.getBalance(worker.getIdWorker());

        for (GiftCodeRecord nextCode : giftCodes) {
            if (creditBalance == 0) {
                break;
            }
            if (nextCode.getAmount() <= creditBalance) {
                payedCodes.add(nextCode);
                creditBalance -= nextCode.getAmount();
            }
        }

        if (creditBalance >= payOffThreshold && creditBalance >= 15 && !payedCodes.isEmpty()) {
            notificationText = notificationText.append("A worker has pending Payments in the amount of ").append(creditBalance).append("ct. Please add giftcodes, so the payment of the worker can be continued.").append(System.getProperty("line.separator"));
        }
        return payedCodes;
    }

    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) throws MoneyTransferException {
        if (!giftCodes.isEmpty()) {
            StringBuilder paymentMessage = loadMessage("src/main/resources/PaymentMessage.txt");
            StringBuilder giftCodeMessage = new StringBuilder();

            //saves payment to the database
            for (GiftCodeRecord rec : giftCodes) {
                workerBalanceOperations.addDebit(worker.getIdWorker(), rec.getAmount(), rec.getIdGiftCode());
                giftCodeMessage.append(rec.getCode()).append(System.getProperty("line.separator"));
            }

            //creates payment message
            Map<String, String> map = new HashMap<>();
            map.put("GiftCodes", giftCodeMessage.toString());
            paymentMessage = new StringBuilder(Template.apply(paymentMessage.toString(), map));

            //sends payment message
            try {
                mailHandler.sendMail(worker.getEmail(), "Your payment for your Crowdworking", paymentMessage.toString());
            } catch (MessagingException e) {
                throw new MoneyTransferException("The MailHandler couldnt send mails to crowdworkers." +
                        "It seems, that there is either a problem with the server or with the properties file.");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e);
            }
        }
    }

    private void sendNotification() throws MoneyTransferException {
        StringBuilder message = new StringBuilder();
        message.append("Dear administrator, ").append(System.getProperty("line.separator"));
        message.append("we want to give you the following information:").append(System.getProperty("line.separator"));
        message.append(notificationText);

        try {
            if (notificationText.length() != 0) {
                LOGGER.trace("Started sending a notification about problems with submission of giftcodes.");
                mailHandler.sendMail(notificationMailAddress, "Payment Notification", message.toString());
                LOGGER.trace("Completed sending a notification about problems with submission of giftcodes.");
            }
        } catch (MessagingException e) {
            throw new MoneyTransferException("The MailHandler couldnt send mails to the administrator." +
                    "It seems, that there is either a problem with the server or with the properties file.");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e);
        }
    }

    private void sendErrorMessage(String errorMessage) {
        StringBuilder message = new StringBuilder();
        message.append("Dear administrator, ").append(System.getProperty("line.separator"));
        message.append("it seems, that a critical error during payment occured:").append(System.getProperty("line.separator"));
        message.append(errorMessage);

        try {
            mailHandler.sendMail(notificationMailAddress, "Payment Error occured", errorMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.error(e);
        }
    }

    private StringBuilder loadMessage(String path) throws MoneyTransferException {
        StringBuilder content = new StringBuilder();

        try {
            FileReader file = new FileReader(path);
            BufferedReader reader = new BufferedReader(file);
            String messageLine;
            while ((messageLine = reader.readLine()) != null) {
                content.append(messageLine);
                content.append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException e) {
            throw new MoneyTransferException("The file at \"" + path + "\" couldn't be found. Please secure, that there is a file.");
        } catch (IOException e) {
            throw new MoneyTransferException("The file at \"" + path + "\" couldn't be read. Please secure, that the file isn't corrupt");
        }

        return content;
    }
}