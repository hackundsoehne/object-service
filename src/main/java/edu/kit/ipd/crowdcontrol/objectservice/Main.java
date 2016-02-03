package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.PlatformManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseMaintainer;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Router;
import edu.kit.ipd.crowdcontrol.objectservice.rest.resources.*;
import org.jooq.SQLDialect;

import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author Niklas Keller
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();

        try (InputStream in = Main.class.getResourceAsStream("/config.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Function<String, String> trimIfNotNull = s -> {
            if (s != null) {
                return s.trim();
            } else {
                return null;
            }
        };

        String url = trimIfNotNull.apply(properties.getProperty("database.url"));
        String username = trimIfNotNull.apply(properties.getProperty("database.username"));
        String password = trimIfNotNull.apply(properties.getProperty("database.password"));
        String databasePool = trimIfNotNull.apply(properties.getProperty("database.poolName"));

        String readOnlyUsername = trimIfNotNull.apply(properties.getProperty("database.readonly.username"));
        String readOnlyPassword = trimIfNotNull.apply(properties.getProperty("database.readonly.password"));

        String dbIntervalString = trimIfNotNull.apply(properties.getProperty("database.maintainer.interval"));
        int dbInterval;
        if (dbIntervalString != null) {
            dbInterval = Integer.parseInt(dbIntervalString);
        } else {
            dbInterval = 24;
        }

        SQLDialect dialect = SQLDialect.valueOf(properties.getProperty("database.dialect").trim());
        DatabaseManager databaseManager;

        try {
            databaseManager = new DatabaseManager(username, password, url, databasePool, dialect);
            databaseManager.initDatabase();
            boot(databaseManager, readOnlyUsername, readOnlyPassword, dbInterval);
        } catch (NamingException | SQLException e) {
            System.err.println("Unable to establish database connection.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void boot(DatabaseManager databaseManager, String readOnlyDBUser, String readOnlyDBPassword, int cleanupInterval) throws SQLException {
        PlatformManager platformManager = null; // TODO

        TemplateOperations templateOperations = new TemplateOperations(databaseManager.getContext());
        NotificationOperations notificationRestOperations = new NotificationOperations(databaseManager, readOnlyDBUser, readOnlyDBPassword);
        PlatformOperations platformOperations = new PlatformOperations(databaseManager.getContext());
        WorkerOperations workerOperations = new WorkerOperations(databaseManager.getContext());
        CalibrationOperations calibrationOperations = new CalibrationOperations(databaseManager.getContext());
        ExperimentOperations experimentOperations = new ExperimentOperations(databaseManager.getContext());
        TagConstraintsOperations tagConstraintsOperations = new TagConstraintsOperations(databaseManager.getContext());
        AlgorithmOperations algorithmsOperations = new AlgorithmOperations(databaseManager.getContext());
        WorkerCalibrationOperations workerCalibrationOperations = new WorkerCalibrationOperations(databaseManager.getContext());
        AnswerRatingOperations answerRatingOperations = new AnswerRatingOperations(databaseManager.getContext(), calibrationOperations, workerCalibrationOperations);

        DatabaseMaintainer maintainer = new DatabaseMaintainer(databaseManager.getContext(), cleanupInterval);
        maintainer.start();

        new Router(
                new TemplateResource(templateOperations),
                new NotificationResource(notificationRestOperations),
                new PlatformResource(platformOperations),
                new WorkerResource(workerOperations, platformManager),
                new CalibrationResource(calibrationOperations),
                new ExperimentResource(experimentOperations, calibrationOperations, tagConstraintsOperations, algorithmsOperations),
                new AlgorithmResources(algorithmsOperations),
                new AnswerRatingResource(experimentOperations, answerRatingOperations, workerOperations),
                new WorkerCalibrationResource(workerCalibrationOperations)
        ).init();
    }
}
