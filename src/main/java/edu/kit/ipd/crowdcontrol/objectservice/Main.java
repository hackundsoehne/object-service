package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.config.Credentials;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PaymentJob;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.fallback.FallbackWorker;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseMaintainer;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.moneytransfer.MoneyTransferManager;
import edu.kit.ipd.crowdcontrol.objectservice.payment.PaymentDispatcher;
import edu.kit.ipd.crowdcontrol.objectservice.quality.QualityIdentificator;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Router;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ho.yaml.Yaml;
import org.jooq.SQLDialect;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.naming.NamingException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * @author Niklas Keller
 */
public class Main {
    private static final Logger LOGGER = LogManager.getRootLogger();

    static {
        // Disable jOOQ's self-advertising
        // http://stackoverflow.com/a/28283538/2373138
        System.setProperty("org.jooq.no-logo", "true");
    }

    public static void main(String[] args) throws IOException, ConfigException {
        LOGGER.trace("Entering application.");

        Config config = Yaml.loadType(Main.class.getResourceAsStream("/config.yml"), Config.class);

        if (config.database.maintainInterval == 0)
            config.database.maintainInterval = 24;
        else if (config.database.maintainInterval < 0)
            throw new ConfigException("negativ maintainInterval of database is not valid");

        SQLDialect dialect = SQLDialect.valueOf(config.database.dialect);
        DatabaseManager databaseManager;

        List<Platform> platforms = new ArrayList<>();

        for (ConfigPlatform platform : config.platforms) {
            platform.type = platform.type.toLowerCase();
            Platform platformInstance;
            switch (platform.type) {
                case "mturk":
                    //FIXME remove the sandbox url - but I am to paranoid
                    platformInstance = new MturkPlatform(platform.user,
                            platform.password,
                            "https://mechanicalturk.sandbox.amazonaws.com/",
                            platform.name,
                            config.deployment.workerService);
                    break;
                case "pybossa":
                    //TODO someone needs to implement pybossa SIIIIMON
                    throw new IllegalArgumentException("Nonono we cannot do this now.");
                case "dummy":
                    platformInstance = new DummyPlatform(platform.name);
                    break;
                default:
                    throw new ConfigException("Platform type \"" + platform.type + "\" not found");
            }
            platforms.add(platformInstance);
        }

        try {
            databaseManager = new DatabaseManager(
                    config.database.writing.user,
                    config.database.writing.password,
                    config.database.url,
                    config.database.databasepool,
                    dialect);

            databaseManager.initDatabase();

            boot(
                    databaseManager, platforms,
                    config.database.readonly,
                    config.database.maintainInterval,
                    config.deployment.origin,
                    config.moneytransfer.notificationMailAddress,
                    config.moneytransfer.parsingPassword,
                    config.moneytransfer.scheduleInterval,
                    config.moneytransfer.payOffThreshold
            );
        } catch (NamingException | SQLException e) {
            System.err.println("Unable to establish database connection.");
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException | MessagingException e) {
            System.err.println("Unable to configure the mailhandler.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void boot(DatabaseManager databaseManager, List<Platform> platforms, Credentials readOnly, int cleanupInterval, String origin, String moneytransferMailAddress, String moneytransferPassword, int moneytransferScheduleIntervalDays, int moneyTransferPayOffThreshold) throws SQLException, IOException, MessagingException {
        TemplateOperations templateOperations = new TemplateOperations(databaseManager.getContext());
        NotificationOperations notificationRestOperations = new NotificationOperations(databaseManager, readOnly.user, readOnly.password);
        PlatformOperations platformOperations = new PlatformOperations(databaseManager.getContext());
        WorkerOperations workerOperations = new WorkerOperations(databaseManager.getContext());
        CalibrationOperations calibrationOperations = new CalibrationOperations(databaseManager.getContext());
        ExperimentOperations experimentOperations = new ExperimentOperations(databaseManager.getContext());
        TagConstraintsOperations tagConstraintsOperations = new TagConstraintsOperations(databaseManager.getContext());
        AlgorithmOperations algorithmsOperations = new AlgorithmOperations(databaseManager.getContext());
        WorkerCalibrationOperations workerCalibrationOperations = new WorkerCalibrationOperations(databaseManager.getContext());
        AnswerRatingOperations answerRatingOperations = new AnswerRatingOperations(databaseManager.getContext(), calibrationOperations, workerCalibrationOperations, experimentOperations);
        TasksOperations tasksOperations = new TasksOperations(databaseManager.getContext());
        WorkerBalanceOperations workerBalanceOperations = new WorkerBalanceOperations(databaseManager.getContext());

        DatabaseMaintainer maintainer = new DatabaseMaintainer(databaseManager.getContext(), cleanupInterval);
        maintainer.start();

        Properties properties = new Properties();
        String mailPropertiesPath = "src/main/resources/mailConfig.properties";

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(mailPropertiesPath))) {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.warn(mailPropertiesPath + " not found, falling back to environment variables for Travis …");

            if (System.getenv("MAIL_USERNAME") == null) {
                LOGGER.error("MAIL_USERNAME not set …");
                System.exit(-2);
            }

            if (System.getenv("MAIL_PASSWORD") == null) {
                LOGGER.error("MAIL_PASSWORD not set …");
                System.exit(-2);
            }

            properties.setProperty("username", System.getenv("MAIL_USERNAME"));
            properties.setProperty("password", System.getenv("MAIL_PASSWORD"));
            properties.setProperty("sender", System.getenv("MAIL_USERNAME"));
            properties.setProperty("mail.smtp.host", "smtp.gmail.com");
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.tls", "true");
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
            properties.setProperty("mail.store.protocol", "imap");
            properties.setProperty("mail.imap.host", "imap.gmail.com");
            properties.setProperty("mail.imap.port", "993");
            properties.setProperty("mail.imap.ssl", "true");
            properties.setProperty("mail.imap.ssl.enable", "true");
            properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        MailHandler mailHandler = new MailHandler(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("username"), properties.getProperty("password"));
            }
        });

        MoneyTransferManager mng = new MoneyTransferManager(mailHandler, workerBalanceOperations, workerOperations, moneytransferMailAddress, moneytransferPassword, moneytransferScheduleIntervalDays, moneyTransferPayOffThreshold);
        mng.start();

        Payment payment = (id, experiment, paymentJob) -> {
            for (PaymentJob job : paymentJob) {
                mng.addMoneyTransfer(job.getWorkerRecord().getIdWorker(), job.getAmount(), experiment.getId());
            }
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.complete(Boolean.TRUE);
            return future;
        };

        PlatformManager platformManager = new PlatformManager(platforms, new FallbackWorker(), payment, tasksOperations, platformOperations,
                workerOperations);
        ExperimentResource experimentResource = new ExperimentResource(experimentOperations, calibrationOperations, tagConstraintsOperations, algorithmsOperations, tasksOperations, platformManager);

        QualityIdentificator qualityIdentificator = new QualityIdentificator(algorithmsOperations, answerRatingOperations, experimentOperations, experimentResource);
        PaymentDispatcher paymentDispatcher = new PaymentDispatcher(platformManager, answerRatingOperations, workerOperations);

        // TODO initialize mailHandler
//        NotificationController notificationController = new NotificationController(notificationRestOperations,
//                new SQLEmailNotificationPolicy(mailHandler, notificationRestOperations));
//        notificationController.init();

        new Router(
                new TemplateResource(templateOperations),
                new NotificationResource(notificationRestOperations),
                new PlatformResource(platformOperations),
                new WorkerResource(workerOperations, platformManager),
                new CalibrationResource(calibrationOperations),
                experimentResource, new AlgorithmResources(algorithmsOperations),
                new AnswerRatingResource(experimentOperations, answerRatingOperations, workerOperations),
                new WorkerCalibrationResource(workerCalibrationOperations),
                origin
        ).init();
    }
}
