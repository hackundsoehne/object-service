package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.CrowdComputingController;
import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;

import java.sql.SQLException;

/**
 * starts everything in the correct order.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("start crowdControl with: username password databaseURL");
        }
        String username = args[0];
        String password = args[1];
        String databaseURL =  args[2];
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
