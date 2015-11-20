package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * manages all the requests which interact with a Crowd-Computing Platform.
 * @author LeanderK
 * @version 1.0
 */
public class CrowdComputingController implements ControllerHelper {
    private final DSLContext create;
    private final Experiment experiment = Tables.EXPERIMENT;
    private final GsonBuilder gsonBuilder =  new GsonBuilder();

    public CrowdComputingController(DSLContext create) {
        this.create = create;
    }

    public Response startExperiment(Request request, Response response) {
        String expID = assertParameter(request, "expID");
        response.status(200);
        response.type("text/plain");
        ExperimentRecord experimentRecord = new ExperimentRecord();
        experimentRecord.setTitel(expID);
        experimentRecord.setRunning(true);
        int i = create.update(experiment)
                .set(experimentRecord)
                .where(experiment.QUESTION.isNotNull())
                .and(experiment.TASKQUESTION.isNotNull())
                .and(experiment.TASK_DESCRIPTION.isNotNull())
                .and(experiment.HIT_TITLE.isNotNull())
                .and(experiment.HIT_DESCRIPTION.isNotNull())
                .and(experiment.BASICPAYMENTHIT.isNotNull())
                .and(experiment.BASICPAYMENTANSWER.isNotNull())
                .and(experiment.BASICPAYMENTRATING.isNotNull())
                .and(experiment.BONUSPAYMENT.isNotNull())
                .and(experiment.MAXANSWERSPERASSIGNMENT.isNotNull())
                .and(experiment.MAXRATINGSPERASSIGNMENT.isNotNull())
                .and(experiment.BUDGET.isNotNull())
                .and(experiment.RUNNING.eq(false))
                .execute();
        if (i == 1) {
            response.body("experiment " + expID + " started");
            //TODO: connect with mTurk
        } else {
            response.body("experiment " + expID + " is already or does not fulfill requirements");
        }
        return response;
    }

    public Response getRunning(Request request, Response response) {
        List<String> running = create.select(experiment.TITEL)
                .where(experiment.RUNNING.eq(true))
                .fetch()
                .map(Record1::value1);
        String json = gsonBuilder.create().toJson(running);
        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

    public Response stopExperiment(Request request, Response response) {
        String expID = assertParameter(request, "expID");
        ExperimentRecord experimentRecord = new ExperimentRecord();
        experimentRecord.setTitel(expID);
        experimentRecord.setRunning(false);
        int affected = create.update(experiment)
                .set(experimentRecord)
                .where(experiment.RUNNING.eq(false))
                .execute();
        if (affected == 1) {
            response.body("stopped " + expID);
        } else {
            response.body(expID + " is not running");
        }
        response.status(200);
        response.type("text/plain");
        return response;
    }
}
