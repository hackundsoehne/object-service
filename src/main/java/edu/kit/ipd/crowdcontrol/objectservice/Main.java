package edu.kit.ipd.crowdcontrol.objectservice;

import com.google.gson.JsonElement;
import edu.kit.ipd.crowdcontrol.objectservice.config.*;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PaymentJob;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.fallback.FallbackWorker;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa.PyBossaPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseMaintainer;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.mail.*;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.moneytransfer.MoneyTransferManager;
import edu.kit.ipd.crowdcontrol.objectservice.notification.NotificationController;
import edu.kit.ipd.crowdcontrol.objectservice.notification.SQLEmailNotificationPolicy;
import edu.kit.ipd.crowdcontrol.objectservice.payment.PaymentDispatcher;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.quality.QualityIdentificator;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Router;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.ho.yaml.Yaml;
import org.jooq.SQLDialect;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.naming.NamingException;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
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

        Config config = getConfig();

        config.log.forEach((key, value) -> {
            Configurator.setLevel(key, Level.getLevel(value));
        });

        if (config.database.maintainInterval == 0)
            config.database.maintainInterval = 24;
        else if (config.database.maintainInterval < 0)
            throw new ConfigException("negative maintainInterval of database is not valid");

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
                            config.deployment.workerService,
                            config.deployment.workerUIPublic);
                    break;
                case "pybossa":
                    if (config.deployment.workerUILocal == null) {
                        config.deployment.workerUILocal = config.deployment.workerUIPublic;
                    }
                    PyBossaPlatform pyBossa = new PyBossaPlatform(config.deployment.workerService,
                            config.deployment.workerUIPublic,
                            config.deployment.workerUILocal,
                            platform.apiKey,
                            platform.url,
                            platform.name,
                            platform.projectId,
                            platform.calibrationsAllowed);
                    pyBossa.init();
                    platformInstance = pyBossa;
                    break;
                case "dummy":
                    platformInstance = new DummyPlatform(platform.name);
                    break;
                default:
                    throw new ConfigException("Platform type \"" + platform.type + "\" not found");
            }
            platforms.add(platformInstance);
        }

        boolean disabledMail = false;

        if (config.mail != null) {
            disabledMail = config.mail.disabled;
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
                    config.moneytransfer.parsingPassword,
                    config.moneytransfer.scheduleInterval,
                    config.moneytransfer.payOffThreshold,
                    disabledMail,
                    config.deployment.port
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

    public static Config getConfig() throws FileNotFoundException {
        InputStream configStream;
        if (System.getProperty("objectservice.config") != null) {
            LOGGER.debug("loading configuration from location: {}", System.getProperty("objectservice.config"));
            configStream = new FileInputStream(System.getProperty("objectservice.config"));
        } else {
            configStream = Main.class.getResourceAsStream("/config.yml");
        }
        Config config = Yaml.loadType(configStream, Config.class);
        if (System.getProperty("workerservice.url") != null) {
            config.deployment.workerService = System.getProperty("workerservice.url");
        }
        if (System.getProperty("origin.url") != null) {
            config.deployment.origin = System.getProperty("origin.url");
        }
        if (System.getProperty("workeruipublic.url") != null) {
            config.deployment.workerUIPublic = System.getProperty("workeruipublic.url");
        }
        if (System.getProperty("workeruilocal.url") != null) {
            config.deployment.workerUILocal = System.getProperty("workeruilocal.url");
        }
        config.platforms = Arrays.stream(config.platforms)
                .filter(platform -> !Boolean.getBoolean(platform.name+".disabled"))
                .toArray(ConfigPlatform[]::new);
        return config;
    }

    private static void boot(DatabaseManager databaseManager, List<Platform> platforms, Credentials readOnly, int cleanupInterval, String origin, String moneytransferPassword, int moneytransferScheduleIntervalDays, int moneyTransferPayOffThreshold, boolean mailDisabled, int port) throws SQLException, IOException, MessagingException {
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
        ExperimentsPlatformOperations experimentsPlatformOperations = new ExperimentsPlatformOperations(databaseManager.getContext());
        WorkerBalanceOperations workerBalanceOperations = new WorkerBalanceOperations(databaseManager.getContext());

        DatabaseMaintainer maintainer = new DatabaseMaintainer(databaseManager.getContext(), cleanupInterval);
        maintainer.start();

        MailFetcher mailFetcher = getMailFetcher(mailDisabled, getConfig().mail.moneyReceiver);

        MailSender mailSenderMoneyTransfer = getMailSender(mailDisabled, getConfig().mail.moneytransfer);
        MoneyTransferManager mng = new MoneyTransferManager(mailFetcher, mailSenderMoneyTransfer, workerBalanceOperations, workerOperations, getConfig().mail.moneytransfer.from, moneytransferPassword, moneytransferScheduleIntervalDays, moneyTransferPayOffThreshold);
        mng.start();

        // notifications might as well use another sendMail instance
        MailSender mailSenderNotification = getMailSender(mailDisabled, getConfig().mail.notifications);
        NotificationController notificationController = new NotificationController(notificationRestOperations,
                new SQLEmailNotificationPolicy(mailSenderNotification, notificationRestOperations));
        notificationController.init();

        Payment payment = new Payment() {
            @Override
            public CompletableFuture<Boolean> payExperiment(int id, JsonElement data, Experiment experiment, List<PaymentJob> paymentJob) {
                for (PaymentJob job : paymentJob) {
                    mng.addMoneyTransfer(job.getWorkerRecord().getIdWorker(), job.getAmount(), experiment.getId());
                }
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                future.complete(Boolean.TRUE);
                return future;
            }

            @Override
            public int getCurrency() {
                //EUR
                return 978;
            }
        };

        PlatformManager platformManager = new PlatformManager(platforms, new FallbackWorker(), payment, experimentsPlatformOperations, platformOperations,
                workerOperations);
        ExperimentResource experimentResource = new ExperimentResource(answerRatingOperations, experimentOperations, calibrationOperations, tagConstraintsOperations, algorithmsOperations, experimentsPlatformOperations, platformManager);

        QualityIdentificator qualityIdentificator = new QualityIdentificator(algorithmsOperations, answerRatingOperations, experimentOperations, experimentResource);
        PaymentDispatcher paymentDispatcher = new PaymentDispatcher(platformManager, answerRatingOperations, workerOperations);

        new Router(
                new TemplateResource(templateOperations),
                new NotificationResource(notificationRestOperations),
                new PlatformResource(platformOperations),
                new WorkerResource(workerOperations, platformManager),
                new CalibrationResource(calibrationOperations),
                experimentResource, new AlgorithmResources(algorithmsOperations),
                new AnswerRatingResource(experimentOperations, answerRatingOperations, workerOperations),
                new WorkerCalibrationResource(workerCalibrationOperations),
                origin,
                port
        ).init();
    }

    private static MailFetcher getMailFetcher(boolean mailDisabled, MailReceiver receiver) throws MessagingException {
        if (mailDisabled) {
            return new CommandLineMailHandler();
        }
        return new MailHandler(MailHandler.Protocol.valueOf(receiver.protocol),
                receiver.auth.credentials.user,
                receiver.auth.credentials.password,
                receiver.auth.server,
                receiver.auth.port);
    }

    private static MailSender getMailSender(boolean mailDisabled, edu.kit.ipd.crowdcontrol.objectservice.config.MailSender sender) {
        if (mailDisabled) {
            return new CommandLineMailHandler();
        }
        return new MailSend(MailSend.Protocol.valueOf(sender.protocol),
                sender.auth.credentials.user,
                sender.auth.credentials.password, "",
                sender.auth.server,
                sender.auth.port);
    }
}
