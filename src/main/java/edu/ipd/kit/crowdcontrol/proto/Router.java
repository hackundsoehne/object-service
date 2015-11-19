package edu.ipd.kit.crowdcontrol.proto;

import spark.servlet.SparkApplication;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Router implements SparkApplication {
    public void init() {
        get("/experiments/delete/:expID", (request, response) -> {
            //delete exp
            return null;
        });

        post("/experiments/create/:expID", (request, response) -> {
            //create exp
            return null;
        });

        get("/experiments/:expID", (request, response) -> {
            //get exp
            return null;
        });

    }
}
