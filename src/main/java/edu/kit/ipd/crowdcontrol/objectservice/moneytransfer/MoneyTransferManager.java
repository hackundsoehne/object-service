package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Result;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Describes a Manager for money transfers. The Manager can log payments and pay off.
 *
 * @author Felix Rittler
 */
public class MoneyTransferManager {

    private static final Logger LOGGER = LogManager.getLogger(MoneyTransferManager.class);
    private final MailSender mailSender;
    private final MailFetcher mailFetcher;
    private final WorkerBalanceOperations workerBalanceOperations;
    private final WorkerOperations workerOperations;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> schedule = null;

    private StringBuilder notificationText;
    private static final String MAIL_FAILURE_MESSAGE = "The MailHandler was not able to send mails." +
            "It seems, that there is either a problem with the server or with the properties file.";

    //config-file data
    private final int payOffThreshold;
    private final String parsingPassword;
    private final String notificationMailAddress;
    private final int scheduleIntervalDays;

    /**
     * Creates a new instance of the MoneyTransferManager
     *
     * @param mailFetcher             the mailfetcher, used to fetch new giftcodes
     * @param mailSender              the mailsender, used send notification and payment messages
     * @param workerBalanceOperations the workerBalanceOperations, used to change the credit balance of a worker
     * @param workerOperations        the workerOperations, used to do operations on workers
     * @param notificationMailAddress the mail address to send notifications
     */
    public MoneyTransferManager(MailFetcher mailFetcher, MailSender mailSender, WorkerBalanceOperations workerBalanceOperations, WorkerOperations workerOperations, String notificationMailAddress, String parsingPassword, int scheduleIntervalDays, int payOffThreshold) {
        this.mailFetcher = mailFetcher;
        this.mailSender = mailSender;
        this.workerOperations = workerOperations;
        this.workerBalanceOperations = workerBalanceOperations;
        this.payOffThreshold = payOffThreshold;
        this.notificationMailAddress = notificationMailAddress;
        this.notificationText = new StringBuilder();
        this.parsingPassword = parsingPassword;
        this.scheduleIntervalDays = scheduleIntervalDays;
    }

    /**
     * Starts the MoneyTransferManager, so giftcodes become submitted to workers every few days.
     */
    public synchronized void start() {
        if (schedule != null) {
            throw new IllegalStateException("run() was called twice!");
        }

        Runnable runnable = () -> {
            notificationText = new StringBuilder();
            try {
                submitGiftCodes();
            } catch (MoneyTransferException e) {
                try {
                    sendCriticalNotification(e.toString());
                } catch (MoneyTransferException f) {
                    LOGGER.error("", f);
                }
                LOGGER.error("", e);
            }
        };

        schedule = scheduler.scheduleAtFixedRate(runnable, 0, scheduleIntervalDays, TimeUnit.DAYS);

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
     *
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    protected void submitGiftCodes() throws MoneyTransferException {
        fetchNewGiftCodes();

        int threshold;
        if (payOffThreshold > 15) {
            threshold = payOffThreshold;
        } else {
            threshold = 15;
        }
        LOGGER.trace("Started submission of giftcodes to workers.");
        Result<WorkerRecord> workers = workerOperations.getWorkerWithCreditBalanceGreaterOrEqual(threshold);
        List<GiftCodeRecord> giftCodes = workerBalanceOperations.getUnusedGiftCodes();

        for (WorkerRecord worker : workers) {
            if (!worker.getEmail().equals("")) {
                List<GiftCodeRecord> payedCodesForWorker = chooseGiftCodes(worker, giftCodes);

                payWorker(worker, payedCodesForWorker);
                giftCodes = workerBalanceOperations.getUnusedGiftCodes();
            }
        }

        //sends a notification about problems with submission of giftcodes
        sendNotification(notificationText.toString());

        LOGGER.trace("Completed submission of giftcodes to workers.");
    }

    /**
     * Fetches new giftcodes from the mailbox and saves them in the database.
     *
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    private void fetchNewGiftCodes() throws MoneyTransferException {
        LOGGER.trace("Started fetching new giftcodes.");

        Message[] messages;

        //fetch new mails
        try {
            messages = mailFetcher.fetchUnseen("inbox");
        } catch (MessagingException e) {
            throw new MoneyTransferException("The MailHandler couldn't fetch new giftcodes from the mailserver. " +
                    "It seems, that there is either a problem with the server or with the properties file.");
        }

        int giftCodesCount = 0;
        //extract giftcodes and save them to the database
        for (Message message : messages) {
            try {
                Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(message, parsingPassword);
                if (rec.isPresent()) {
                    workerBalanceOperations.addGiftCode(rec.get().getCode(), rec.get().getAmount());
                    giftCodesCount++;
                }
            } catch (MoneyTransferException e) {
                try {
                    mailFetcher.markAsUnseen(message);
                    throw new MoneyTransferException(e.getMessage());
                } catch (MessagingException f) {

                    throw new MoneyTransferException(e.getMessage() + System.getProperty("line.separator") + "The MailHandler was unable to revert all changes and mark the read mails as unseen." +
                            "It seems, that there is either a problem with the server or with the properties file.");
                }
            }
        }
        LOGGER.trace("Completed fetching " + giftCodesCount + " new giftcodes.");
    }

    /**
     * Chooses the giftcodes, a worker receives as payment.
     *
     * @param worker    the worker to pay
     * @param giftCodes all giftcodes
     * @return returns the list of the chosen giftcodes
     */
    private List<GiftCodeRecord> chooseGiftCodes(WorkerRecord worker, List<GiftCodeRecord> giftCodes) {

        LOGGER.trace("Started to choose the giftcodes worker " + worker.getIdWorker() + " will receive.");

        int creditBalanceAtStart = workerBalanceOperations.getBalance(worker.getIdWorker());

        int[] weights = new int[creditBalanceAtStart + 1];
        boolean[][] decision = new boolean[giftCodes.size()][creditBalanceAtStart + 1];


        for (int i = 0; i < giftCodes.size(); i++) {
            int weight = giftCodes.get(i).getAmount();

            for(int j = creditBalanceAtStart; j >= weight; j--) {

                if (weights[j-weight] + weight > weights[j]) {
                    weights[j] = weights[j - weight] + weight;
                    decision[i][j] = true;
                }
            }
        }

        int capacity = creditBalanceAtStart;
        boolean[] x = new boolean[giftCodes.size()];

        for (int i = giftCodes.size() - 1; i >= 0; i--) {
            x[i] = decision[i][capacity];
            if (x[i]) {
                capacity = capacity - giftCodes.get(i).getAmount();
            }
        }

        int creditBalance = creditBalanceAtStart;
        List<GiftCodeRecord> payedCodes = new ArrayList<>();
        Iterator<GiftCodeRecord> it = giftCodes.iterator();

        for (boolean aX : x) {
            GiftCodeRecord rec = it.next();
            if (aX) {
                payedCodes.add(rec);
                creditBalance -= rec.getAmount();
            }
        }

        if (creditBalance >= payOffThreshold && creditBalance >= 15) {
            notificationText.append("A worker has pending Payments in the amount of ").append(creditBalance).append("ct. Please add giftcodes, so the payment of the worker can be continued.").append(System.getProperty("line.separator"));
        }

        LOGGER.trace("Calculation completed: Worker " + worker.getIdWorker() + " will receive " + payedCodes.size() + " giftcodes with a total amount of " + (creditBalanceAtStart - creditBalance) + "ct.");
        return payedCodes;
    }

    /**
     * Builds the payment message and sends giftcodes to the worker.
     *
     * @param worker    the worker to pay
     * @param giftCodes the giftcodes to send
     * @throws MoneyTransferException gets thrown, if an error occured
     */
    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) throws MoneyTransferException {
        LOGGER.trace("Started to send " + giftCodes.size() + " giftcodes to worker" + worker.getIdWorker() + ".");
        if (giftCodes.isEmpty()) {
            return;
        }
        String paymentMessage = loadMessage("src/main/resources/moneytransfer/PaymentMessage.txt");
        StringBuilder giftCodeMessage = new StringBuilder();

        for (GiftCodeRecord rec : giftCodes) {
            giftCodeMessage.append(rec.getCode()).append(System.getProperty("line.separator"));
        }

        //creates payment message
        Map<String, String> map = new HashMap<>();
        map.put("GiftCodes", giftCodeMessage.toString());
        paymentMessage = Template.apply(paymentMessage, map);

        //sends payment message and saves the giftcodes to the database
        try {
            mailSender.sendMail(worker.getEmail(), "Your payment for your Crowdworking", paymentMessage);
            for (GiftCodeRecord rec : giftCodes) {
                workerBalanceOperations.addDebit(worker.getIdWorker(), rec.getAmount(), rec.getIdGiftCode());
            }
            LOGGER.trace("Completed sending of " + giftCodes.size() + " giftcodes to worker" + worker.getIdWorker() + ".");
        } catch (MessagingException e) {
            throw new MoneyTransferException(MAIL_FAILURE_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Sending of " + giftCodes.size() + " gift codes to worker" + worker.getIdWorker() + " failed.", e);
        }
    }

    /**
     * Sends a notification about problems during payment to the administrator.
     *
     * @param message the message to send
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    private void sendNotification(String message) throws MoneyTransferException {
        String subject = "Payment Notification";
        StringBuilder mail;
        try {
            mail = new StringBuilder(loadMessage("src/main/resources/moneytransfer/notificationMessage.txt"));
        } catch (MoneyTransferException e) {
            mail = new StringBuilder(e.toString());
        }

        mail = mail.append(message);
        try {
            if (notificationText.length() != 0) {
                LOGGER.trace("Started sending a notification about problems with submission of giftcodes.");
                mailSender.sendMail(notificationMailAddress, subject, mail.toString());
                LOGGER.trace("Completed sending a notification about problems with submission of giftcodes.");
            }
        } catch (MessagingException e) {
            throw new MoneyTransferException(MAIL_FAILURE_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * Sends a notification about errors during payment to the administrator.
     *
     * @param message the message to send
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    private void sendCriticalNotification(String message) throws MoneyTransferException {
        String subject = "Payment Error occurred";
        StringBuilder mail;
        try {
            mail = new StringBuilder(loadMessage("src/main/resources/moneytransfer/errorMessage.txt"));
        } catch (MoneyTransferException e) {
            mail = new StringBuilder(e.toString());
        }

        mail = mail.append(message);

        try {
            if (notificationText.length() != 0) {
                LOGGER.trace("Started sending a notification about errors with submission of giftcodes.");
                mailSender.sendMail(notificationMailAddress, subject, mail.toString());
                LOGGER.trace("Completed sending a notification about errors with submission of giftcodes.");
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * Loads a string from a file.
     *
     * @param path the path to the file
     * @return the StringBuilder to return
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    private String loadMessage(String path) throws MoneyTransferException {
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

        return content.toString();
    }
}