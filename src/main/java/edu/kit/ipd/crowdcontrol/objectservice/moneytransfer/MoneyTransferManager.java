package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.Main;
import edu.kit.ipd.crowdcontrol.objectservice.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.jooq.Result;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final String MAIL_FAILURE_MESSAGE = "The MailHandler was not able to send mails." +
            "It seems, that there is either a problem with the server or with the properties file.";
    private final MailSender mailSender;
    private final MailFetcher mailFetcher;
    private final WorkerBalanceOperations workerBalanceOperations;
    private final WorkerOperations workerOperations;
    private final PlatformOperations platformOperations;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //config-file data
    private final int payOffThreshold;
    private final String parsingPassword;
    private final String notificationMailAddress;
    private final int scheduleIntervalDays;
    private ScheduledFuture<?> schedule = null;
    private StringBuilder notificationText;
    private Map<String, BigDecimal> exchangeRates;

    /**
     * Creates a new instance of the MoneyTransferManager
     *
     * @param mailFetcher             the mailfetcher, used to fetch new giftcodes
     * @param mailSender              the mailsender, used send notification and payment messages
     * @param workerBalanceOperations the workerBalanceOperations, used to change the credit balance of a worker
     * @param workerOperations        the workerOperations, used to do operations on workers
     * @param notificationMailAddress the mail address to send notifications
     */
    public MoneyTransferManager(MailFetcher mailFetcher, MailSender mailSender, WorkerBalanceOperations workerBalanceOperations, WorkerOperations workerOperations, PlatformOperations platformOperations, String notificationMailAddress, String parsingPassword, int scheduleIntervalDays, int payOffThreshold) {
        this.mailFetcher = mailFetcher;
        this.mailSender = mailSender;
        this.workerOperations = workerOperations;
        this.workerBalanceOperations = workerBalanceOperations;
        this.platformOperations = platformOperations;
        this.payOffThreshold = payOffThreshold;
        this.notificationMailAddress = notificationMailAddress;
        this.notificationText = new StringBuilder();
        this.parsingPassword = parsingPassword;
        this.scheduleIntervalDays = scheduleIntervalDays;
        this.exchangeRates = new HashMap<>();
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
                    sendNotification(NotificationLevel.ERROR,e.toString());
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
        //reset all exchange rates
        exchangeRates = new HashMap<>();

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
        sendNotification(NotificationLevel.ISSUE,notificationText.toString());
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
            messages = mailFetcher.fetchUnseen();
        } catch (MessagingException e) {
            throw new MoneyTransferException("The MailHandler couldn't fetch new giftcodes from the mailserver. " +
                    "It seems, that there is either a problem with the server or with the properties file.", e);
        }

        int giftCodesCount = 0;
        //extract giftcodes and save them to the database
        Map<Integer, Integer> countKeys = new HashMap<>();
        Stack<Integer> amountList = new Stack<>();
        for (Message message : messages) {
            Optional<GiftCodeRecord> rec = MailParser.parseAmazonGiftCode(message, parsingPassword);
            if (rec.isPresent()) {
                GiftCodeRecord giftCode = rec.get();
                workerBalanceOperations.addGiftCode(giftCode.getCode(), giftCode.getAmount());
                if (!countKeys.containsKey(giftCode.getAmount())) {
                    amountList.push(giftCode.getAmount());
                    countKeys.put(giftCode.getAmount(), 1);
                } else {
                    countKeys.put(giftCode.getAmount(), countKeys.get(giftCode.getAmount()) + 1);
                }
                giftCodesCount++;
            }
            try {
                mailFetcher.markAsSeen(message);
            } catch (MessagingException e) {
                throw new MoneyTransferException("The mail fetcher could not mark a mail as seen after extracting giftcode and addition to database", e);
            }
        }

        //Create notification
        StringBuilder message = new StringBuilder();
        for (Integer amount : amountList) {
            message.append("Count of keys: ").append(countKeys.get(amount)).append(" Value of giftcodes: ").append(amount).append(System.getProperty("line.separator"));
        }

        if (!message.toString().equals("")) {
            sendNotification(NotificationLevel.GIFTCODES_ADDED, message.toString());
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
    private List<GiftCodeRecord> chooseGiftCodes(WorkerRecord worker, List<GiftCodeRecord> giftCodes) throws MoneyTransferException {

        LOGGER.trace("Started to choose the giftcodes worker " + worker.getIdWorker() + " will receive.");

        int currCodeWorker = platformOperations.getPlatformRecord(worker.getPlatform()).orElseThrow(() -> new MoneyTransferException("Platform of worker " + worker.getIdWorker() + " cannot be found.")).getCurrency();

        for (GiftCodeRecord giftcode : giftCodes) {
            giftcode.setAmount(exchangeTo(giftcode.getAmount(),giftcode.getCurrency(), currCodeWorker));
            giftcode.setCurrency(978);
        }

        int creditBalanceAtStart = workerBalanceOperations.getBalance(worker.getIdWorker());

        int[] weights = new int[creditBalanceAtStart + 1];
        boolean[][] decision = new boolean[giftCodes.size()][creditBalanceAtStart + 1];

        for (int i = 0; i < giftCodes.size(); i++) {
            int weight = giftCodes.get(i).getAmount();

            for (int j = creditBalanceAtStart; j >= weight; j--) {

                if (weights[j - weight] + weight > weights[j]) {
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
        String paymentMessage = Utils.loadFile("/moneytransfer/PaymentMessage.txt");
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
            throw new MoneyTransferException(MAIL_FAILURE_MESSAGE, e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Sending of " + giftCodes.size() + " gift codes to worker" + worker.getIdWorker() + " failed.", e);
        }
    }

    /**
     * Sends a notification with information about payment to the administrator.
     *
     * @param message the message to send
     * @throws MoneyTransferException gets thrown, if an error occurred
     */
    private void sendNotification(NotificationLevel level, String message) throws MoneyTransferException {
        Properties properties = new Properties();
        try {
            InputStreamReader reader;
            switch (level) {
                case ERROR:
                    reader = new InputStreamReader(Main.class.getResourceAsStream("/moneytransfer/errorNotification.properties"));
                    break;
                case ISSUE:
                    reader = new InputStreamReader(Main.class.getResourceAsStream("/moneytransfer/issueNotification.properties"));
                    break;
                case GIFTCODES_ADDED:
                    reader  = new InputStreamReader(Main.class.getResourceAsStream("/moneytransfer/giftcodesAddedNotification.properties"));
                    break;
                default:
                    //cannot happen
                    reader = null;
            }
            properties.load(reader);
        } catch (IOException e) {
            throw new MoneyTransferException("Error while loading properties for a notification.", e);
        }
        String subject = properties.getProperty("subject");
        String mail = Utils.loadFile(properties.getProperty("pathToMessage"));

        Map<String, String> map = new HashMap<>();
        map.put("content", message);
        mail = Template.apply(mail, map);
        try {
            if (mail.length() != 0) {
                LOGGER.trace("Started sending a notification about " + properties.getProperty("loggerMessage") + ".");
                mailSender.sendMail(notificationMailAddress, subject, mail);
                LOGGER.trace("Completed sending a notification about " + properties.getProperty("loggerMessage") + ".");
            }
        } catch (MessagingException e) {
            throw new MoneyTransferException(MAIL_FAILURE_MESSAGE, e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("", e);
        }
    }

    protected static BigDecimal getExchangeRate(String sourceCurrency, String destinationCurrency) throws MoneyTransferException {
        LOGGER.trace("Started fetching currency exchange rates from " + sourceCurrency + " to " + destinationCurrency + ".");

        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("http://quote.yahoo.com/d/quotes.csv?s=" + sourceCurrency + destinationCurrency + "=X&f=l1&e=.csv");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String responseBody;
        try {
            responseBody = httpclient.execute(httpGet, responseHandler);
            httpclient.close();
        } catch (IOException e) {
            LOGGER.error("Fetching currency exchange rates failed.");
            throw new MoneyTransferException("There was an error while getting exchange rates from " + sourceCurrency + " to " + destinationCurrency + ".", e);
        }

        BigDecimal rate = new BigDecimal(responseBody.substring(0, responseBody.length() - 2));

        boolean isEurUsdConversion = false;
        if ((sourceCurrency.equals("EUR") && destinationCurrency.equals("USD")) || (sourceCurrency.equals("USD") && destinationCurrency.equals("EUR"))) {
            isEurUsdConversion = true;
        }

        if (rate.compareTo(new BigDecimal(2)) == 1 && isEurUsdConversion) {
            throw new MoneyTransferException("Exchange rate in an EUR/USD conversion is above 2. The exchange rate must not be above 2.");
        } else if (rate.compareTo(new BigDecimal("0.5")) == -1 && isEurUsdConversion) {
            throw new MoneyTransferException("Exchange rate in an EUR/USD conversion is below 0.5. The exchange rate must not be below 0.5.");
        }

        LOGGER.trace("Completed fetching currency exchange rates.");

        return rate;
    }

    private int exchangeTo(int amount, int sourceCurrencyCode, int destCurrencyCode) throws MoneyTransferException {
        CurrencyUnit sourceCurr = CurrencyUnit.ofNumericCode(sourceCurrencyCode);
        CurrencyUnit destCurr = CurrencyUnit.ofNumericCode(destCurrencyCode);

        Money money = Money.zero(sourceCurr);
        money = money.plusMinor(amount);
        BigDecimal exchangeRate;
        String key = Integer.toString(sourceCurrencyCode) + Integer.toString(destCurrencyCode);
        if (exchangeRates.containsKey(key)) {
            exchangeRate = exchangeRates.get(key);
        } else {
            exchangeRate = getExchangeRate(sourceCurr.getCurrencyCode(), destCurr.getCurrencyCode());
            exchangeRates.put(key, exchangeRate);
        }
        Money converted = money.convertedTo(destCurr, exchangeRate, RoundingMode.HALF_UP);
        return converted.getAmountMinorInt();
    }

    private enum NotificationLevel {
        ERROR, ISSUE, GIFTCODES_ADDED
    }

}