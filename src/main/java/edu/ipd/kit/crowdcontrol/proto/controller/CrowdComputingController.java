package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.Hit;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.HitType;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONHitAnswer;
import org.jooq.DSLContext;
import org.jooq.Record1;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * manages all the requests which interact with a Crowd-Computing Platform.
 * @author LeanderK
 * @version 1.0
 */
public class CrowdComputingController implements ControllerHelper {
    private final DSLContext create;
    private final Experiment experiment = Tables.EXPERIMENT;
    private final GsonBuilder gsonBuilder =  new GsonBuilder();
    private final CrowdPlatformManager crowdPlatformManager;

    public CrowdComputingController(DSLContext create, CrowdPlatformManager crowdPlatformManager) {
        this.create = create;
        this.crowdPlatformManager = crowdPlatformManager;
    }

    //TODO: pretty shitty method, needs refacturing
    public Response startExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        int payment = assertParameterInt(request, "payment");
        int amount = assertParameterInt(request, "amount");
        String platformAnswer = assertParameter(request, "platformAnswer");
        String platformRating = assertParameter(request, "platformRating");
        int ratingToAnswer = assertParameterInt(request, "RatingToAnswer");
        crowdPlatformManager.getCrowdplatform(platformAnswer)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("platformAnswer "  + platformAnswer + " not found"));
        crowdPlatformManager.getCrowdplatform(platformRating)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("platformRating "  + platformRating + " not found"));

        List<String> tags = create.select(Tables.TAGS.TAG)
                .where(Tables.TAGS.EXPERIMENT_T.eq(expID))
                .fetch(Record1::value1);

        //TODO: connect with webview!
        Function<ExperimentRecord, Stream<Hit>> expRecordToHit = record -> Stream.of(HitType.ANSWER, HitType.RATING)
                .map(type -> new Hit(record, type, tags, type.getAmount(amount, ratingToAnswer), payment, 24*60*60, 30*24*60*60, ""));

        Function<Hit, CompletableFuture<Hit>> publishHit = hit -> hit.getHitType()
                .map(type -> type.getPlatform(platformAnswer, platformRating))
                .flatMap(crowdPlatformManager::getCrowdplatform)
                .map(platform -> platform.publishTask(hit))
                .orElseThrow(() -> new InternalServerErrorException("platform for hit " + hit + " is not available"));

        Stream<CompletableFuture<Hit>> publishingHits = create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
                .fetchOptional()
                .map(expRecordToHit::apply)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("ExperimentTable " + expID + " not found"))
                .map(publishHit::apply);



        response.status(200);
        response.type("text/plain");
        return response;
    }

    public Response getRunning(Request request, Response response) {
        return createJson(request, response, () -> {
            List<JSONHitAnswer> map = create.select()
                    .from(Tables.HIT)
                    .join(Tables.EXPERIMENT).onKey()
                    .where(Tables.HIT.RUNNING.eq(true))
                    .fetch()
                    .map(record -> new JSONHitAnswer(record.getValue(Tables.EXPERIMENT.TITEL), record.into(Tables.HIT)));
            return gsonBuilder.create().toJson(map);
        });
    }

    public Response stopExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        response.status(200);
        response.type("text/plain");
        return response;
    }
}
