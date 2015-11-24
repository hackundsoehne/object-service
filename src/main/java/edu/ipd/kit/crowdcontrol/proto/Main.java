package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;
import edu.ipd.kit.crowdcontrol.proto.controller.StatisticsController;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.MTurkPlatform;
import edu.ipd.kit.crowdcontrol.proto.web.FreeMarkerEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 * starts everything in the correct order.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class Main {
    private final DatabaseManager databaseManager;
    private final CrowdPlatformManager crowdPlatformManager;
    private final Router router;

    public Main(String configFile) {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(configFile));
        } catch (IOException e) {
            System.err.println("unable to open file: " + configFile);
            System.exit(-1);
        }

        databaseManager = initDatabase(config);
        crowdPlatformManager = initCrowdPlatform(config);
        router = initRouter(config);
    }

    private Router initRouter(Properties config) {
        ExperimentController experimentController = new ExperimentController(databaseManager.getContext(), crowdPlatformManager);
        String urlString = config.getProperty("url");
        if (urlString == null) {
            System.err.println("missing url property");
            System.exit(-1);
        }
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.err.println("url is malformed");
            System.exit(-1);
        }
        CrowdComputingController crowdComputingController = new CrowdComputingController(databaseManager.getContext(), crowdPlatformManager, url);
        StatisticsController statisticsController = new StatisticsController(databaseManager.getContext());
        TaskController taskController = new TaskController(databaseManager.getContext());
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
        Router router = new Router(experimentController, crowdComputingController, taskController, statisticsController);
        router.init();
        return router;
    }

    private CrowdPlatformManager initCrowdPlatform(Properties config) {
        String mUsername = config.getProperty("mTurkUsername");
        String mPassword = config.getProperty("mTurkPassword");
        String mDatabaseURL =  config.getProperty("mTurkURL");
        MTurkPlatform mTurkPlatform =  new MTurkPlatform(mUsername, mPassword, mDatabaseURL);
        return new CrowdPlatformManager(Arrays.asList(mTurkPlatform));
    }

    private DatabaseManager initDatabase(Properties config) {
        String username = config.getProperty("username");
        String password = config.getProperty("password");
        String databaseURL =  config.getProperty("databaseURL");

        if (username.isEmpty() || password.isEmpty() || databaseURL.isEmpty()) {
            System.err.println("start crowdControl with: username password databaseURL");
            System.exit(-1);
        }
        try {
            return new DatabaseManager(username, password, databaseURL);
        } catch (SQLException e) {
            System.err.println("unable to initialize the database");
            e.printStackTrace();
            System.exit(-1);
            throw new IllegalStateException();
        }
    }

    public static void main(String[] args) {
        String configFile;
        if (args.length != 0) {
            configFile = args[0];
        } else {
            configFile = "ccp.properties";
        }
        new Main(configFile);
    }

}
