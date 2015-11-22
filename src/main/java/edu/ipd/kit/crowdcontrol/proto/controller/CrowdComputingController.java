package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.Hit;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.HitType;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONHitAnswer;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final URL baseURL;

    public CrowdComputingController(DSLContext create, CrowdPlatformManager crowdPlatformManager, URL baseURL) {
        this.create = create;
        this.crowdPlatformManager = crowdPlatformManager;
        this.baseURL = baseURL;
    }

    //TODO: pretty shitty method, needs refacturing
    public Response startHIT(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        int payment = assertParameterInt(request, "payment");
        int amount = assertParameterInt(request, "amount");
        String platformAnswer = assertParameter(request, "platformAnswer");
        String platformRating = assertParameter(request, "platformRating");
        int ratingToAnswer = assertParameterInt(request, "RatingToAnswer");
        crowdPlatformManager.getCrowdPlatform(platformAnswer)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("platformAnswer "  + platformAnswer + " not found"));
        crowdPlatformManager.getCrowdPlatform(platformRating)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("platformRating "  + platformRating + " not found"));

        Map<String, HitRecord> inserted = new HashMap<>();
        create.transaction(config -> {
            DSL.using(config).select(Tables.EXPERIMENT.IDEXPERIMENT)
                    .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
                    .fetchOptional()
                    .orElseThrow(() -> new ResourceNotFoundExcpetion("table "  + expID + " not found"));

            List<HitRecord> collect = Stream.of(HitType.ANSWER, HitType.RATING)
                    .map(type -> new HitRecord(null, expID, type.name(), true, 0, type.getAmount(amount, ratingToAnswer),
                            payment, 0, null, type.getPlatform(platformAnswer, platformRating)))
                    .collect(Collectors.toList());

            Map<String, HitRecord> result = DSL.using(config)
                    .insertInto(Tables.HIT)
                    .set(collect.get(0))
                    .newRecord()
                    .set(collect.get(1))
                    .returning()
                    .fetch()
                    .intoMap(Tables.HIT.TYPE);

            inserted.putAll(result);
        });

        List<String> tags = create.select(Tables.TAGS.TAG)
                .where(Tables.TAGS.EXPERIMENT_T.eq(expID))
                .fetch(Record1::value1);

        Function<ExperimentRecord, Stream<Hit>> expRecordToHit = record -> Stream.of(HitType.ANSWER, HitType.RATING)
                .map(type -> new Hit(record, type, tags, type.getAmount(amount, ratingToAnswer), payment, 24*60*60, 30*24*60*60, getExternalURL(inserted, type)));


        List<CompletableFuture<Hit>> publishingHits = create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
                .fetchOptional()
                .map(expRecordToHit::apply)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("ExperimentTable " + expID + " not found"))
                .map(hit -> crowdPlatformManager.publishHit(hit, platformAnswer, platformRating, (hitR, ex) -> storeOrDelete(inserted, hit, ex)))
                .collect(Collectors.toList());

        try {
            CompletableFuture
                    .allOf(publishingHits.toArray(new CompletableFuture[publishingHits.size()]))
                    .get(1, TimeUnit.SECONDS);
            response.body("success");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("an error occurred");
        }

        response.status(200);
        response.type("text/plain");
        return response;
    }

    private String getExternalURL(Map<String, HitRecord> inserted, HitType type) {
        String route = null;
        if (type == HitType.ANSWER) {
            route = "answer";
        } else {
            route = "rating";
        }
        try {
            return Unirest.get(baseURL.toExternalForm()+"/{type}/render")
                    .routeParam("type", route)
                    .header("accept", "text/html")
                    .queryString("hitID", inserted.get(type.name()).getIdhit())
                    .asString().getBody();
        } catch (UnirestException e) {
            throw new InternalServerErrorException("unable to build hit url", e);
        }
    }

    private void storeOrDelete(Map<String, HitRecord> inserted, Hit hit, Throwable ex) {
        if (ex == null) {
            create.executeUpdate(inserted.get(hit.getHitType().get().name()));
        } else {
            create.executeDelete(inserted.get(hit.getHitType().get().name()));
        }
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

    public Response stopHIT(Request request, Response response) {
        int idHit = assertParameterInt(request, "idHit");

        CompletableFuture<String> result = create.transactionResult(conf -> {
            Boolean running = DSL.using(conf)
                    .select(Tables.HIT.RUNNING)
                    .where(Tables.HIT.IDHIT.eq(idHit))
                    .fetchOptional()
                    .orElseThrow(() -> new ResourceNotFoundExcpetion("Unable to find HIT " + idHit))
                    .value1();

            if (!running)
                throw new ResourceNotFoundExcpetion("HIT is not running anymore " + idHit);

            HitRecord hitRecord = create.update(Tables.HIT)
                    .set(Tables.HIT.IDHIT, idHit)
                    .set(Tables.HIT.RUNNING, false)
                    .returning()
                    .fetchOptional()
                    .orElseThrow(() -> new ResourceNotFoundExcpetion("Unable to find HIT " + idHit));

            return crowdPlatformManager.getCrowdPlatform(hitRecord.getCrowdPlatform())
                    .orElseThrow(() -> new InternalServerErrorException("crowd-platform " + hitRecord.getCrowdPlatform() + " is not present"))
                    .unpublishTask(hitRecord.getIdCrowdPlatform());
        });

        try {
            result.get(1, TimeUnit.SECONDS);
            response.body("success");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("an error occurred");
        }

        response.status(200);
        response.type("text/plain");
        return response;
    }
}
