package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * starts everything in the correct order.
 * @author LeanderK
 * @version 1.0
 */
public class Main {
    public static final Properties config = new Properties();
    public static void main(String[] args) {
        try {
            config.load(new FileInputStream("ccp.properties"));
        } catch (IOException e) {
            System.out.println("Failed to load config!");
        }

        if (args.length != 3) {
            System.err.println("start crowdControl with: username password databaseURL");
        }
        String username = config.getProperty("username");
        String password = config.getProperty("password")
        String databaseURL =  config.getProperty("databaseURL");

        if (username.isEmpty() || password.isEmpty() || databaseURL.isEmpty()) {
            System.err.println("start crowdControl with: username password databaseURL");
        }
        DatabaseManager databaseManager = null;
        try {
            databaseManager = new DatabaseManager(username, password, databaseURL);
        } catch (SQLException e) {
            System.err.println("unable to initialize the database");
            e.printStackTrace();
            System.exit(-1);
        }
        ExperimentController experimentController = new ExperimentController(databaseManager.getContext());
        CrowdComputingController crowdComputingController = new CrowdComputingController(databaseManager.getContext());
        Router router = new Router(experimentController, crowdComputingController);
        router.init();
    }

}
