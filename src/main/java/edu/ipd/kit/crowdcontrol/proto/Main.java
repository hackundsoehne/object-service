package edu.ipd.kit.crowdcontrol.proto;

import edu.ipd.kit.crowdcontrol.proto.controller.ExperimentController;

import java.sql.SQLException;

/**
 * starts everything in the correct order.
 * @author LeanderK
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        String username = args[0];
        String password = args[1];
        String databaseURL =  args[2];
        DatabaseManager databaseManager = null;
        try {
            databaseManager = new DatabaseManager(username, password, databaseURL);
        } catch (SQLException e) {
            System.err.println("unable to initialize the database");
            e.printStackTrace();
            System.exit(-1);
        }
        ExperimentController experimentController = new ExperimentController(databaseManager.getContext());
        Router router = new Router(experimentController);
        router.init();
    }
}
