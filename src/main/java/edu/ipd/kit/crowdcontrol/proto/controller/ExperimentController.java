package edu.ipd.kit.crowdcontrol.proto.controller;

import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.ExperimentDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.QualificationsRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.TagsRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONExperiment;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * manages all the requests which are used to specify an experiment.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class ExperimentController extends Controller {
    private final ExperimentDao experimentDao;
    private final CrowdPlatformManager crowdPlatformManager;

    public ExperimentController(DSLContext create, CrowdPlatformManager crowdPlatformManager) {
        super(create);
        this.crowdPlatformManager = crowdPlatformManager;
        experimentDao = new ExperimentDao(create.configuration());
    }

    public Response createExperiment(Request request, Response response) {
        return processJsonWithTransaction(request, response, (raw, config) -> {
            JSONExperiment json = gson.fromJson(raw, JSONExperiment.class);
            response.body("error: experiment is already existing");
            response.type("text/plain");
            ExperimentRecord expRecord = json.createRecord();
            int execute = DSL.using(config)
                    .insertInto(Tables.EXPERIMENT)
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
        return processJsonWithTransaction(request, response, (raw, conf) -> {
            JSONExperiment json = gson.fromJson(raw, JSONExperiment.class);
            ExperimentRecord newRecord = json.createRecord();
            String shoudUpdate = request.params("update");

            List<Hit> hitsToUpdate = getExperimentAndTags(conf, newRecord.getIdexperiment())
                    .map((exp, tags) -> DSL.using(conf)
                            .selectFrom(Tables.HIT)
                            .where(Tables.HIT.RUNNING.eq(true))
                            .and(Tables.HIT.EXPERIMENT_H.eq(exp.getIdexperiment()))
                            .fetch()
                            .stream()
                            .map(hitRecord -> new Hit(exp, hitRecord, tags))
                            .filter(hit -> hit.needsUpdate(newRecord, json.getTags()))
                            .map(hit -> hit.update(newRecord, json.getTags()))
                            .collect(Collectors.toList())
                    );

            if (!hitsToUpdate.isEmpty() && (shoudUpdate == null || shoudUpdate.equals("false"))) {
                response.body("error: there are hits running. Add param update=true to update hits");
                return;
            }

            DSL.using(conf)
                    .update(Tables.EXPERIMENT)
                    .set(newRecord)
                    .returning()
                    .fetchOptional()
                    .ifPresent(experimentRecord -> {
                        DSL.using(conf)
                                .deleteFrom(Tables.TAGS)
                                .where(Tables.TAGS.EXPERIMENT_T.eq(experimentRecord.getIdexperiment()))
                                .execute();
                        DSL.using(conf)
                                .deleteFrom(Tables.TAGS)
                                .where(Tables.TAGS.EXPERIMENT_T.eq(experimentRecord.getIdexperiment()))
                                .execute();
                        DSL.using(conf)
                                .batchInsert(json.getTags())
                                .execute();
                        DSL.using(conf)
                                .batchInsert(json.getQualifications())
                                .execute();

                        CompletableFuture[] updates = hitsToUpdate.stream()
                                .map(crowdPlatformManager::updateHit)
                                .toArray(CompletableFuture[]::new);

                        String body = CompletableFuture.allOf(updates)
                                .handle((result, ex) -> {
                                    if (result != null) {
                                        return "success";
                                    } else {
                                        ex.printStackTrace();
                                        return "error: an error occurred while trying to update running tasks";
                                    }
                                }).join();
                        response.body(body);
                    });
        });
    }

    private Tuple2<ExperimentRecord, Result<TagsRecord>> getExperimentAndTags(Configuration conf, int ID) {
        ExperimentRecord exp = DSL.using(conf)
                .selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(ID))
                .fetchOptional()
                .orElseThrow(() -> new ResourceNotFoundExcpetion("Table " + ID + "is not existing yet."));

        Result<TagsRecord> tags = DSL.using(conf)
                .selectFrom(Tables.TAGS)
                .where(Tables.TAGS.EXPERIMENT_T.eq(ID))
                .fetch();

        return Tuple.tuple(exp, tags);
    }

    public Response deleteExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        int affected = create.deleteFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
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
        ExperimentRecord experimentRecord = create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
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
