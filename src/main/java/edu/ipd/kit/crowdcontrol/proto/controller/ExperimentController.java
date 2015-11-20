package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratingoptions;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.RatingoptionsRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONExperiment;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * manages all the requests which are used to specify an experiment.
 * @author LeanderK
 * @version 1.0
 */
public class ExperimentController implements ControllerHelper {
    private final DSLContext create;
    private final Experiment experiment = Tables.EXPERIMENT;
    private final GsonBuilder gsonBuilder =  new GsonBuilder();

    public ExperimentController(DSLContext create) {
        this.create = create;
    }

    public Response createExperiment(Request request, Response response) {
        assertJson(request);
        String json = request.body();
        Gson gson = gsonBuilder.create();
        JSONExperiment jsonExperiment = gson.fromJson(json, JSONExperiment.class);
        ExperimentRecord expRecord = jsonExperiment.createRecord();
        create.transaction(config -> {
            int execute = DSL.using(config)
                            .insertInto(experiment)
                            .set(expRecord)
                            .execute();
            if (execute == 1) {
                DSL.using(config)
                        .batchInsert(jsonExperiment.getQualifications());
                DSL.using(config)
                        .batchInsert(jsonExperiment.getTags());
            }
        });

        response.status(200);
        response.body("affected=" + (affectedExp + affectedRatings));
        response.type("text/plain");
        return response;
    }

    public Response deleteExperiment(Request request, Response response) {
        String expID = assertParameter(request, "expID");
        int affected = create.deleteFrom(experiment)
                .where(experiment.TITEL.eq(request.params(expID)))
                .and(experiment.RUNNING.eq(false))
                .execute();
        response.status(200);
        response.body("deleted=" + affected);
        response.type("text/plain");
        return response;
    }

    public Response getExperiment(Request request, Response response) {
        String expID = assertParameter(request, "expID");
        ExperimentRecord experimentRecord = create.selectFrom(experiment)
                .where(experiment.TITEL.eq(expID))
                .fetchOne();
        Map<String, Double> ratingOptionsM = create.select(ratingOptions.KEY, ratingOptions.VALUE)
                .where(ratingOptions.EXPERIMENT.eq(experimentRecord.getId()))
                .fetch()
                .intoMap(ratingOptions.KEY, ratingOptions.VALUE);

        JSONExperiment experiment = new JSONExperiment(experimentRecord, ratingOptionsM);
        String json = gsonBuilder.create().toJson(experiment);
        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

}
