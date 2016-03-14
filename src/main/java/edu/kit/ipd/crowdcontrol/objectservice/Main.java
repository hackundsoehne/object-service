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
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailReceiver;
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
import org.jooq.DSLContext;
import org.jooq.SQLDialect;

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

    static class OperationCarrier {
        public final TemplateOperations templateOperations;
        public final NotificationOperations notificationRestOperations;
        public final PlatformOperations platformOperations;
        public final WorkerOperations workerOperations;
        public final CalibrationOperations calibrationOperations;
        public final ExperimentOperations experimentOperations;
        public final TagConstraintsOperations tagConstraintsOperations;
        public final AlgorithmOperations algorithmsOperations;
        public final WorkerCalibrationOperations workerCalibrationOperations;
        public final AnswerRatingOperations answerRatingOperations;
        public final ExperimentsPlatformOperations experimentsPlatformOperations;
        public final WorkerBalanceOperations workerBalanceOperations;

        public OperationCarrier(Config config, DatabaseManager manager) throws SQLException {
            DSLContext ctx = manager.getContext();
            templateOperations = new TemplateOperations(ctx);
            notificationRestOperations = new NotificationOperations(manager, config.database.readonly.user, config.database.readonly.password);
            platformOperations = new PlatformOperations(ctx);
            workerOperations = new WorkerOperations(ctx);
            calibrationOperations = new CalibrationOperations(ctx);
            experimentOperations = new ExperimentOperations(ctx);
            tagConstraintsOperations = new TagConstraintsOperations(ctx);
            algorithmsOperations = new AlgorithmOperations(ctx);
            workerCalibrationOperations = new WorkerCalibrationOperations(ctx);
            answerRatingOperations = new AnswerRatingOperations(ctx, calibrationOperations, workerCalibrationOperations, experimentOperations);
            experimentsPlatformOperations = new ExperimentsPlatformOperations(ctx);
            workerBalanceOperations = new WorkerBalanceOperations(ctx);
        }
    }

    static {
        // Disable jOOQ's self-advertising
        // http://stackoverflow.com/a/28283538/2373138
        System.setProperty("org.jooq.no-logo", "true");
    }

    public static void main(String[] args) throws IOException, ConfigException, SQLException {
        LOGGER.trace("Entering application.");

        Config config = getConfig();

        initLogLevel(config);

        configValidate(config);
        List<Platform> platforms = getPlatforms(config);

        DatabaseManager databaseManager = initDatabase(config);

        OperationCarrier operationCarrier = new OperationCarrier(config, databaseManager);

        MailSender moneyTransferSender = getMailSender(config.mail.disabled, config.mail.moneytransfer, config.mail.debug);
        MailFetcher moneyTransferFetcher = getMailFetcher(config.mail.disabled, config.mail.moneyReceiver, config.mail.debug);
        MoneyTransferManager moneyTransfer = initMoneyTransfer(config, operationCarrier, moneyTransferFetcher, moneyTransferSender);

        MailSender notificationSender = getMailSender(config.mail.disabled, config.mail.notifications, config.mail.debug);
        NotificationController notificationController = initNotificationController(operationCarrier, notificationSender);

        PlatformManager platformManager = initPlatformManager(operationCarrier, platforms, moneyTransfer);

        //FIXME this should NEVER be here, we have to find a better way on doing this
        ExperimentResource experimentResource = new ExperimentResource(
                operationCarrier.answerRatingOperations,
                operationCarrier.experimentOperations,
                operationCarrier.calibrationOperations,
                operationCarrier.tagConstraintsOperations,
                operationCarrier.algorithmsOperations,
                operationCarrier.experimentsPlatformOperations,
                platformManager);


        initEventHandler(operationCarrier, platformManager, experimentResource);
        initRouter(config, operationCarrier, platformManager, experimentResource);
    }

    private static void initEventHandler(OperationCarrier operationCarrier, PlatformManager platformManager, ExperimentResource experimentResource) {
        new QualityIdentificator(
                operationCarrier.algorithmsOperations,
                operationCarrier.answerRatingOperations,
                operationCarrier.experimentOperations, experimentResource);

        new PaymentDispatcher(
                platformManager,
                operationCarrier.answerRatingOperations,
                operationCarrier.workerOperations);
    }

    private static void initRouter(Config config, OperationCarrier operationCarrier, PlatformManager platformManager, ExperimentResource experimentResource) {
        new Router(
                new TemplateResource(operationCarrier.templateOperations),
                new NotificationResource(operationCarrier.notificationRestOperations),
                new PlatformResource(operationCarrier.platformOperations),
                new WorkerResource(operationCarrier.workerOperations, platformManager),
                new CalibrationResource(operationCarrier.calibrationOperations),
                experimentResource, new AlgorithmResources(operationCarrier.algorithmsOperations),
                new AnswerRatingResource(operationCarrier.experimentOperations, operationCarrier.answerRatingOperations, operationCarrier.workerOperations),
                new WorkerCalibrationResource(operationCarrier.workerCalibrationOperations),
                config.deployment.origin,
                config.deployment.port
        ).init();
    }

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

    private static NotificationController initNotificationController(OperationCarrier carrier, MailSender notificationSender) {
        // notifications might as well use another sendMail instance
        NotificationController notificationController = new NotificationController(carrier.notificationRestOperations,
                new SQLEmailNotificationPolicy(notificationSender, carrier.notificationRestOperations));
        notificationController.init();

        return notificationController;
    }

    private static MoneyTransferManager initMoneyTransfer(Config config, OperationCarrier operationCarrier, MailFetcher mailFetcher, MailSender mailSender) {
        MoneyTransferManager mng = new MoneyTransferManager(mailFetcher,
                mailSender,
                operationCarrier.workerBalanceOperations,
                operationCarrier.workerOperations,
                config.moneytransfer.notificationMailAddress,
                config.moneytransfer.parsingPassword,
                config.moneytransfer.scheduleInterval,
                config.moneytransfer.payOffThreshold);
        mng.start();

        return mng;
    }

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

    private static void configValidate(Config config) throws ConfigException {
        if (config.database.maintainInterval < 0)
            throw new ConfigException("negative maintainInterval of database is not valid");
    }

    private static void initLogLevel(Config config) {
        config.log.forEach((key, value) -> Configurator.setLevel(key, Level.getLevel(value)));
    }

    /**
     * Load all platforms from the config
     * @param config the config to use to load
     * @return A list of configured and initialized platforms
     * @throws ConfigException if the config contains invalid values.
     */
    private static List<Platform> getPlatforms(Config config) throws ConfigException {
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
        return platforms;
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

    private static MailSender getMailSender(boolean mailDisabled, edu.kit.ipd.crowdcontrol.objectservice.config.MailSender sender, boolean debug) {
        if (mailDisabled || sender == null) {
            return new CommandLineMailHandler();
        }
        return new MailSend(MailSend.Protocol.valueOf(sender.protocol),
                sender.auth.credentials.user,
                sender.auth.credentials.password, "",
                sender.auth.server,
                sender.auth.port,
                debug);
    }
}
