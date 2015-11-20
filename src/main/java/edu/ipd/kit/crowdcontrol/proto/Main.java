package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.MTurkPlatform;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 * starts everything in the correct order.
 * @author LeanderK
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
        }

        databaseManager = initDatabase(config);
        crowdPlatformManager = initCrowdPlatform(config);
        router = initRouter();
    }

    private Router initRouter() {
        ExperimentController experimentController = new ExperimentController(databaseManager.getContext());
        CrowdComputingController crowdComputingController = new CrowdComputingController(databaseManager.getContext(), crowdPlatformManager);
        Router router = new Router(experimentController, crowdComputingController);
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
