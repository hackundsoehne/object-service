package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.QualificationsRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.TagsRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONExperiment;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.stream.Collectors;

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
        response.body("error: experiment is already existing");
        create.transaction(config -> {
            int execute = DSL.using(config)
                            .insertInto(experiment)
                            .set(expRecord)
                            .execute();
            if (execute != 0) {
                DSL.using(config)
                        .batchInsert(jsonExperiment.getQualifications())
                        .execute();
                DSL.using(config)
                        .batchInsert(jsonExperiment.getTags())
                        .execute();
                response.body("success");
            }
        });
        response.status(200);
        response.type("text/plain");
        return response;
    }

    public Response updateExperiment(Request request, Response response) {
        assertJson(request);
        String json = request.body();
        Gson gson = gsonBuilder.create();
        JSONExperiment jsonExperiment = gson.fromJson(json, JSONExperiment.class);
        ExperimentRecord expRecord = jsonExperiment.createRecord();
        response.body("error: experiment is already existing");
        create.transaction(config -> {
            DSL.using(config)
                    .update(experiment)
                    .set(expRecord)
                    .execute();
            Record1<String> existing = create.select(experiment.TITEL)
                    .where(experiment.TITEL.eq(expRecord.getTitel()))
                    .fetchOne();
            if (existing != null) {
                DSL.deleteFrom(Tables.TAGS)
                        .where(Tables.TAGS.EXPERIMENT_T.eq(expRecord.getIdexperiment()))
                        .execute();
                DSL.deleteFrom(Tables.TAGS)
                        .where(Tables.TAGS.EXPERIMENT_T.eq(expRecord.getIdexperiment()))
                        .execute();
                DSL.using(config)
                        .batchInsert(jsonExperiment.getTags())
                        .execute();
                DSL.using(config)
                        .batchInsert(jsonExperiment.getQualifications())
                        .execute();
                response.body("success");
            }
        });
        response.status(200);
        response.type("text/plain");
        return response;
    }

    public Response deleteExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        int affected = create.deleteFrom(experiment)
                .where(experiment.IDEXPERIMENT.eq(expID))
                .execute();
        response.status(200);
        if (affected != 0) {
            response.body("success");
        } else {
            response.body("error");
        }
        response.type("text/plain");
        return response;
    }

    public Response getExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        ExperimentRecord experimentRecord = create.selectFrom(experiment)
                .where(experiment.IDEXPERIMENT.eq(expID))
                .fetchOne();

        if (experimentRecord == null) {
            response.body("error");
            response.type("text/plain");
            return response;
        }

        List<String> qualifications = create.selectFrom(Tables.QUALIFICATIONS)
                .where(Tables.QUALIFICATIONS.EXPERIMENT_Q.eq(expID))
                .fetch()
                .stream()
                .map(QualificationsRecord::getText)
                .collect(Collectors.toList());

        List<String> tags = create.selectFrom(Tables.TAGS)
                .where(Tables.TAGS.EXPERIMENT_T.eq(expID))
                .fetch()
                .stream()
                .map(TagsRecord::getTag)
                .collect(Collectors.toList());


        String json = gsonBuilder.create().toJson(new JSONExperiment(experimentRecord, tags, qualifications));
        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

}
