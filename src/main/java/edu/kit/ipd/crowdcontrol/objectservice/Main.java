package edu.kit.ipd.crowdcontrol.objectservice;

import com.google.gson.JsonElement;
import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.fallback.FallbackWorker;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.local.LocalPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.pybossa.PyBossaPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseMaintainer;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.PopulationsHelper;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.feedback.FeedbackCreator;
import edu.kit.ipd.crowdcontrol.objectservice.mail.*;
import edu.kit.ipd.crowdcontrol.objectservice.moneytransfer.MoneyTransferManager;
import edu.kit.ipd.crowdcontrol.objectservice.notification.NotificationController;
import edu.kit.ipd.crowdcontrol.objectservice.notification.SQLEmailNotificationPolicy;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.DuplicateChecker;
import edu.kit.ipd.crowdcontrol.objectservice.payment.PaymentDispatcher;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.quality.QualityIdentificator;
import edu.kit.ipd.crowdcontrol.objectservice.rest.JWTHelper;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Router;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jooq.SQLDialect;
import spark.Spark;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public static void main(String[] args) throws IOException, ConfigException, SQLException {
        LOGGER.trace("Entering application.");

        ConfigLoader configLoader = new ConfigLoader();
        Config config = configLoader.getConfig();

        initLogLevel(config);

        DatabaseManager databaseManager = initDatabase(config);
        EventManager eventManager = new EventManager();

        OperationCarrier operationCarrier = new OperationCarrier(config, databaseManager);

        List<Platform> platforms = getPlatforms(config, operationCarrier);

        MailSender moneyTransferSender = getMailSender(config.mail.disabled, config.mail.moneytransfer, config.mail.debug);
        MailFetcher moneyTransferFetcher = getMailFetcher(config.mail.disabled, config.mail.moneyReceiver, config.mail.debug);
        MoneyTransferManager moneyTransfer = initMoneyTransfer(config, operationCarrier, moneyTransferFetcher, moneyTransferSender);

        MailSender notificationSender = getMailSender(config.mail.disabled, config.mail.notifications, config.mail.debug);
        NotificationController notificationController = initNotificationController(operationCarrier, notificationSender, eventManager);

        PlatformManager platformManager = initPlatformManager(operationCarrier, platforms, moneyTransfer);


        ExperimentFetcher experimentFetcher = new ExperimentFetcher(operationCarrier.experimentOperations, operationCarrier.experimentsPlatformOperations,operationCarrier.tagConstraintsOperations, operationCarrier.algorithmsOperations, operationCarrier.calibrationOperations);
        DuplicateChecker duplicateChecker = new DuplicateChecker(operationCarrier.answerRatingOperations,operationCarrier.experimentOperations,eventManager);
        ExperimentOperator experimentOperator = new ExperimentOperator(platformManager,experimentFetcher,operationCarrier.experimentsPlatformOperations,eventManager,duplicateChecker,config.deployment.taskWaitBeforeFinish);
        PopulationsHelper populationsHelper = new PopulationsHelper( operationCarrier.calibrationOperations, operationCarrier.experimentsPlatformOperations);

        JWTHelper jwtHelper = new JWTHelper(config.deployment.jwtsecret);

        initEventHandler(operationCarrier, platformManager, experimentOperator, eventManager, experimentFetcher, jwtHelper);
        initRouter(config, operationCarrier, platformManager, experimentOperator, experimentFetcher, populationsHelper, eventManager);

        Spark.awaitInitialization();
    }

    /**
     * Load all modules which are subscribing on events
     * @param operationCarrier DatabaseOperations to use
     * @param platformManager PlatformManager to use
     * @param experimentOperator the operations to use for starting stopping experiments
     * @param eventManager the EventManager to use
     * @param jwtHelper the jwt-helper used to generate the JWT-tokens
     */
    private static void initEventHandler(OperationCarrier operationCarrier, PlatformManager platformManager, ExperimentOperator experimentOperator, EventManager eventManager, ExperimentFetcher experimentFetcher, JWTHelper jwtHelper) {
        FeedbackCreator feedbackCreator = new FeedbackCreator(operationCarrier.answerRatingOperations, operationCarrier.experimentOperations, operationCarrier.workerOperations, jwtHelper);
        new QualityIdentificator(
                operationCarrier.algorithmsOperations,
                operationCarrier.answerRatingOperations,
                operationCarrier.experimentOperations,
                experimentOperator,
                operationCarrier.experimentsPlatformOperations,
                eventManager, experimentFetcher);

        new PaymentDispatcher(
                feedbackCreator,
                platformManager,
                operationCarrier.answerRatingOperations,
                operationCarrier.workerOperations,
                eventManager);

    }

    /**
     * Load and run the Router
     * @param config config to use
     * @param operationCarrier database operations to use
     * @param platformManager the platformManager to run the platformOperations on
     * @param experimentOperator experimentOperations to use
     * @param experimentFetcher the experiment fetcher utility to use
     * @param populationsHelper the populations helper to use
     * @param eventManager EventManager to use
     */
    private static void initRouter(Config config, OperationCarrier operationCarrier, PlatformManager platformManager, ExperimentOperator experimentOperator, ExperimentFetcher experimentFetcher, PopulationsHelper populationsHelper, EventManager eventManager) {
        new Router(
                new TemplateResource(operationCarrier.templateOperations, eventManager),
                new NotificationResource(operationCarrier.notificationRestOperations, eventManager),
                new PlatformResource(operationCarrier.platformOperations),
                new WorkerResource(operationCarrier.workerOperations, platformManager, eventManager),
                new CalibrationResource(operationCarrier.calibrationOperations, eventManager),
                new ExperimentResource(operationCarrier.answerRatingOperations, operationCarrier.experimentOperations, operationCarrier.calibrationOperations, operationCarrier.tagConstraintsOperations, operationCarrier.algorithmsOperations, operationCarrier.experimentsPlatformOperations, platformManager, experimentOperator, experimentFetcher, populationsHelper, eventManager),
                new AlgorithmResources(operationCarrier.algorithmsOperations),
                new AnswerRatingResource(operationCarrier.experimentOperations, operationCarrier.answerRatingOperations, operationCarrier.workerOperations, eventManager),
                new WorkerCalibrationResource(operationCarrier.workerCalibrationOperations, eventManager),
                config.deployment.origin,
                config.deployment.port
        ).init();
    }

    /**
     * Load all configured Platforms into the manager object
     * @param operationCarrier database operations to use
     * @param platforms the platforms to run
     * @param moneyTransferManager the moneytranfermanager to use as fallback payservice
     * @return a running PlatformManager
     */
    private static PlatformManager initPlatformManager(OperationCarrier operationCarrier, List<Platform> platforms, MoneyTransferManager moneyTransferManager) {
        Payment payment = new Payment() {
            @Override
            public CompletableFuture<Boolean> payExperiment(int id, JsonElement data, Experiment experiment, List<PaymentJob> paymentJob) {
                for (PaymentJob job : paymentJob) {
                    moneyTransferManager.addMoneyTransfer(job.getWorkerRecord().getIdWorker(), job.getAmount(), experiment.getId());
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

        return new PlatformManager(platforms, new FallbackWorker(), payment,
                operationCarrier.experimentsPlatformOperations,
                operationCarrier.platformOperations,
                operationCarrier.workerOperations);
    }

    /**
     * InitNotifications
     * @param carrier DatabaseOperations to use
     * @param notificationSender the sender to send the notifications
     * @return A running notification contoller
     */
    private static NotificationController initNotificationController(OperationCarrier carrier, MailSender notificationSender, EventManager eventManager) {
        // notifications might as well use another sendMail instance
        NotificationController notificationController = new NotificationController(carrier.notificationRestOperations,
                new SQLEmailNotificationPolicy(notificationSender, carrier.notificationRestOperations, eventManager), eventManager);
        notificationController.init();

        return notificationController;
    }

    /**
     * Load moneyTransferManager
     * @param config config to use
     * @param operationCarrier database operations to use
     * @param mailFetcher the fetcher to get the giftcodes
     * @param mailSender the sender to send messages with
     *
     * @return running MoneytransferManager
     */
    private static MoneyTransferManager initMoneyTransfer(Config config, OperationCarrier operationCarrier, MailFetcher mailFetcher, MailSender mailSender) {
        MoneyTransferManager mng = new MoneyTransferManager(mailFetcher,
                mailSender,
                operationCarrier.workerBalanceOperations,
                operationCarrier.workerOperations,
                operationCarrier.platformOperations,
                config.mail.admin,
                config.moneytransfer.parsingPassword,
                config.moneytransfer.scheduleInterval,
                config.moneytransfer.payOffThreshold);
        mng.start();

        return mng;
    }

    /**
     * Load Database related stuff and create manager.
     *
     * @param config config to use
     * @return initialized database manager
     */
    private static DatabaseManager initDatabase(Config config) {
        SQLDialect dialect = SQLDialect.valueOf(config.database.dialect);
        DatabaseManager databaseManager = null;
        try {
            databaseManager = new DatabaseManager(
                    config.database.writing.user,
                    config.database.writing.password,
                    config.database.url,
                    config.database.databasepool,
                    dialect);
            databaseManager.initDatabase();

            DatabaseMaintainer maintainer = new DatabaseMaintainer(databaseManager.getContext(), config.database.maintainInterval);
            maintainer.start();
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return databaseManager;
    }

    /**
     * flush log lvls into the logger
     * @param config config to use
     */
    private static void initLogLevel(Config config) {
        config.log.forEach((key, value) -> Configurator.setLevel(key, Level.getLevel(value)));
    }

    /**
     * Load all platforms from the config
     * @param config the config to use to load
     * @return A list of configured and initialized platforms
     * @throws ConfigException if the config contains invalid values.
     */
    private static List<Platform> getPlatforms(Config config, OperationCarrier carrier) throws ConfigException {
        List<Platform> platforms = new ArrayList<>();

        for (ConfigPlatform platform : config.platforms) {
            platform.type = platform.type.toLowerCase();
            Platform platformInstance;
            switch (platform.type) {
                case "mturk":
                    MturkPlatform mturkPlatform;
                    //FIXME remove the sandbox url - but I am to paranoid
                    platformInstance = mturkPlatform = new MturkPlatform(platform.user,
                            platform.password,
                            "https://mechanicalturk.amazonaws.com/",
                            platform.name,
                            config.deployment.workerService,
                            config.deployment.workerUIPublic);
                    List<String> hits = carrier.experimentsPlatformOperations.getIdentificationsFromPlatform(mturkPlatform.getId());
                    mturkPlatform.startExtenderService(hits);

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
                            true);
                    pyBossa.init();
                    platformInstance = pyBossa;
                    break;
                case "dummy":
                    platformInstance = new DummyPlatform(platform.name, platform.url);
                    break;
                case "local":
                    platformInstance = new LocalPlatform(platform.name, platform.url);
                    break;
                default:
                    throw new ConfigException("Platform type \"" + platform.type + "\" not found");
            }
            platforms.add(platformInstance);
        }
        return platforms;


    }

    /**
     * Get a MailFetcher instance for the passed config
     * @param mailDisabled if the mail is disabled
     * @param receiver config to use
     * @param debug if you want to debug your connection
     *
     * @return A MailFetcher instance
     */
    private static MailFetcher getMailFetcher(boolean mailDisabled, edu.kit.ipd.crowdcontrol.objectservice.config.MailReceiver receiver, boolean debug) {
        if (mailDisabled || receiver == null) {
            return new CommandLineMailHandler();
        }
        return new MailReceiver(MailReceiver.Protocol.valueOf(receiver.protocol),
                receiver.auth.credentials.user,
                receiver.auth.credentials.password,
                receiver.auth.server,
                receiver.auth.port,
                receiver.inbox,
                debug);
    }

    /**
     * Get a MailSender instance for the passed config
     * @param mailDisabled if the mail is dissabled
     * @param sender config to use
     * @param debug if you want to debug your connection
     * @return a Mailsender instance to use
     */
    private static MailSender getMailSender(boolean mailDisabled, edu.kit.ipd.crowdcontrol.objectservice.config.MailSender sender, boolean debug) {
        if (mailDisabled || sender == null) {
            return new CommandLineMailHandler();
        }
        return new MailSend(MailSend.Protocol.valueOf(sender.protocol),
                sender.auth.credentials.user,
                sender.auth.credentials.password,
                sender.from,
                sender.auth.server,
                sender.auth.port,
                debug);
    }
}
