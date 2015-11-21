package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.QualificationsRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.TagsRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONExperiment;
import edu.ipd.kit.crowdcontrol.proto.json.JSONRequestChecker;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * manages all the requests which are used to specify an experiment.
 * @author LeanderK
 * @version 1.0
 */
public class ExperimentController implements ControllerHelper {
    private final DSLContext create;
    private final Experiment experiment = Tables.EXPERIMENT;
    private final Gson gson =  new GsonBuilder()
            .registerTypeAdapter(JSONExperiment.class, new JSONRequestChecker<>())
            .create();

    public ExperimentController(DSLContext create) {
        this.create = create;
    }

    public Response createExperiment(Request request, Response response) {
        return processJsonWithTransaction(request, response, (json, config) -> {
            response.body("error: experiment is already existing");
            response.type("text/plain");
            ExperimentRecord expRecord = json.createRecord();
            int execute = DSL.using(config)
                    .insertInto(experiment)
                    .set(expRecord)
                    .execute();
            if (execute != 0) {
                DSL.using(config)
                        .batchInsert(json.getQualifications())
                        .execute();
                DSL.using(config)
                        .batchInsert(json.getTags())
                        .execute();
                response.body("success");
            }
        });
    }

    public Response updateExperiment(Request request, Response response) {
        return processJsonWithTransaction(request, response, (json, conf) -> {
            ExperimentRecord expRecord = json.createRecord();
            //TODO: update current HIT!
            ExperimentRecord existing = create.selectFrom(experiment)
                    .where(experiment.IDEXPERIMENT.eq(expRecord.getIdexperiment()))
                    .fetchOptional()
                    .orElseThrow(() -> new ResourceNotFoundExcpetion("Table " + expRecord.getIdexperiment() + "is not existing yet."));
            DSL.using(conf)
                    .update(experiment)
                    .set(expRecord)
                    .execute();
            Record1<String> existingTitle = create.select(experiment.TITEL)
                    .where(experiment.TITEL.eq(expRecord.getTitel()))
                    .fetchOne();
            if (existingTitle != null) {
                DSL.using(conf)
                        .deleteFrom(Tables.TAGS)
                        .where(Tables.TAGS.EXPERIMENT_T.eq(expRecord.getIdexperiment()))
                        .execute();
                DSL.using(conf)
                        .deleteFrom(Tables.TAGS)
                        .where(Tables.TAGS.EXPERIMENT_T.eq(expRecord.getIdexperiment()))
                        .execute();
                DSL.using(conf)
                        .batchInsert(json.getTags())
                        .execute();
                DSL.using(conf)
                        .batchInsert(json.getQualifications())
                        .execute();
                response.body("success");
            }
        });
    }

    private Response processJsonWithTransaction(Request request, Response response, BiConsumer<JSONExperiment, Configuration> consumer) {
        assertJson(request);
        String json = request.body();
        JSONExperiment jsonExperiment = gson.fromJson(json, JSONExperiment.class);
        response.status(200);
        create.transaction(config -> consumer.accept(jsonExperiment, config));
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
                .fetchOptional()
                .orElseThrow(() -> new BadRequestException("experiment not found"));

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


        String json = gson.toJson(new JSONExperiment(experimentRecord, tags, qualifications));
        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }
}
