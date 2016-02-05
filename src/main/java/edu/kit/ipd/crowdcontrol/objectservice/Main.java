package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.config.Credentials;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.fallback.FallbackWorker;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseMaintainer;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.payment.PaymentDispatcher;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Router;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ho.yaml.Yaml;
import org.jooq.SQLDialect;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                    platformInstance = new MturkPlatform(platform.user, platform.password, "https://mechanicalturk.sandbox.amazonaws.com/");
                    break;
                case "pybossa":
                    //TODO someone needs to implement pybossa SIIIIMON
                    throw new IllegalArgumentException("Nonono we cannot do this now.");
                case "dummy":
                    platformInstance = new DummyPlatform(platform.name);
                    break;
                default:
                    throw new ConfigException("Platform type \""+platform.type+"\" not found");
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
                    config.deployment.origin
            );
        } catch (NamingException | SQLException e) {
            System.err.println("Unable to establish database connection.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void boot(DatabaseManager databaseManager, List<Platform> platforms, Credentials readOnly, int cleanupInterval, String origin) throws SQLException {
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

        DatabaseMaintainer maintainer = new DatabaseMaintainer(databaseManager.getContext(), cleanupInterval);
        maintainer.start();

        PlatformManager platformManager = new PlatformManager(platforms, new FallbackWorker(), null, tasksOperations, platformOperations,
                workerOperations); // TODO set fallbackPayment

        PaymentDispatcher paymentDispatcher = new PaymentDispatcher(platformManager, answerRatingOperations,workerOperations);

        new Router(
                new TemplateResource(templateOperations),
                new NotificationResource(notificationRestOperations),
                new PlatformResource(platformOperations),
                new WorkerResource(workerOperations, platformManager),
                new CalibrationResource(calibrationOperations),
                new ExperimentResource(experimentOperations, calibrationOperations, tagConstraintsOperations, algorithmsOperations),
                new AlgorithmResources(algorithmsOperations),
                new AnswerRatingResource(experimentOperations, answerRatingOperations, workerOperations),
                new WorkerCalibrationResource(workerCalibrationOperations),
                origin
        ).init();
    }
}
