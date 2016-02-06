package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
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
    private MailHandler mailHandler;
    private WorkerBalanceOperations workerBalanceOperations;
    private WorkerOperations workerOperations;
    private int payOffThreshold;
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
                try {
                    sendNotification(e.toString(), true);
                } catch (MoneyTransferException f) {
                    LOGGER.error(f);
                }
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
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    public void submitGiftCodes() throws MoneyTransferException {
        LOGGER.trace("Started fetching new giftcodes.");
        fetchNewGiftCodes();
        LOGGER.trace("Completed fetching new giftcodes.");

        LOGGER.trace("Started submission of giftcodes to workers.");
        Result<WorkerRecord> workers = workerOperations.getWorkerWithCreditBalanceGreaterOrEqual(payOffThreshold);
        List<GiftCodeRecord> giftCodes = workerBalanceOperations.getUnusedGiftCodes();

        for (WorkerRecord worker : workers) {
            if (!worker.getEmail().equals("")) {
                List<GiftCodeRecord> payedCodesForWorker = chooseGiftCodes(worker, giftCodes);

                payWorker(worker, payedCodesForWorker);
                giftCodes = workerBalanceOperations.getUnusedGiftCodes();
            }
        }
        LOGGER.trace("Completed submission of giftcodes to workers.");

        //sends a notification about problems with submission of giftcodes
        sendNotification(notificationText.toString(), false);
    }

    /**
     * Fetches new giftcodes from the mailbox and saves them in the database.
     *
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
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
                Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(message);
                if (rec.isPresent()) {
                    workerBalanceOperations.addGiftCode(rec.get().getCode(), rec.get().getAmount());
                }
            } catch (MoneyTransferException e) {
                try {
                    mailHandler.markAsUnseen(message);
                    throw new MoneyTransferException(e.getMessage());
                } catch (MessagingException f) {
                    throw new MoneyTransferException("The MailHandler couldnt mark mails as unseen." +
                            "It seems, that there is either a problem with the server or with the properties file." + System.getProperty("line.separator") +
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Chooses the giftcodes, a worker receives as payment.
     *
     * @param worker    the worker to pay
     * @param giftCodes all giftcodes
     * @return returns the list of the chosen giftcodes
     */
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

    /**
     * Builds the payment message and sends giftcodes to the worker.
     *
     * @param worker    the worker to pay
     * @param giftCodes the giftcodes to send
     * @throws MoneyTransferException gets thrown, if an error occured
     */
    private void payWorker(WorkerRecord worker, List<GiftCodeRecord> giftCodes) throws MoneyTransferException {
        if (!giftCodes.isEmpty()) {
            StringBuilder paymentMessage = loadMessage("src/main/resources/PaymentMessage.txt");
            StringBuilder giftCodeMessage = new StringBuilder();

            for (GiftCodeRecord rec : giftCodes) {
                giftCodeMessage.append(rec.getCode()).append(System.getProperty("line.separator"));
            }

            //creates payment message
            Map<String, String> map = new HashMap<>();
            map.put("GiftCodes", giftCodeMessage.toString());
            paymentMessage = new StringBuilder(Template.apply(paymentMessage.toString(), map));

            //sends payment message and saves the giftcodes to the database
            try {
                mailHandler.sendMail(worker.getEmail(), "Your payment for your Crowdworking", paymentMessage.toString());
                for (GiftCodeRecord rec : giftCodes) {
                    workerBalanceOperations.addDebit(worker.getIdWorker(), rec.getAmount(), rec.getIdGiftCode());
                }
            } catch (MessagingException e) {
                throw new MoneyTransferException("The MailHandler couldnt send mails to crowdworkers." +
                        "It seems, that there is either a problem with the server or with the properties file.");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e);
            }
        }
    }

    /**
     * Sends a notification about problems or errors during payment to the administrator.
     *
     * @param message the message to send
     * @param criticalError the urgency of the problem
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    private void sendNotification(String message, boolean criticalError) throws MoneyTransferException {
        StringBuilder mail;
        String subject = "";
        try {
            if (criticalError) {
                subject = "Payment Error occurred";
                mail = loadMessage("src/main/resources/errorMoneyTransferMessage.txt");
            } else {
                subject = "Payment Notification";
                mail = loadMessage("src/main/resources/notificationMoneyTransferMessage.txt");
            }
        } catch (MoneyTransferException e) {
            mail = new StringBuilder(e.toString());
        }

        mail = mail.append(message);
        try {
            if (notificationText.length() != 0) {
                LOGGER.trace("Started sending a notification about problems or errors with submission of giftcodes.");
                mailHandler.sendMail(notificationMailAddress, subject, mail.toString());
                LOGGER.trace("Completed sending a notification about problems or errors with submission of giftcodes.");
            }
        } catch (MessagingException e) {
            throw new MoneyTransferException("The MailHandler couldnt send mails to the administrator." +
                    "It seems, that there is either a problem with the server or with the properties file.");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e);
        }
    }


    /**
     * Loads a string from a file.
     *
     * @param path the path to the file
     * @return the StringBuilder to return
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    protected static StringBuilder loadMessage(String path) throws MoneyTransferException {
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