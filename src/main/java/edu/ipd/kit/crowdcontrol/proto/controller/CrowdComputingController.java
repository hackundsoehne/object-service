package edu.ipd.kit.crowdcontrol.proto.controller;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.Hit;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.HitType;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.ExperimentDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.TagsRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONHit;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.DSL;
import spark.Request;
import spark.Response;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * manages all the requests which interact with a Crowd-Computing Platform.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class CrowdComputingController extends Controller {
    private final ExperimentDao experimentDao;
    private final CrowdPlatformManager crowdPlatformManager;
    private final URL baseURL;

    public CrowdComputingController(DSLContext create, CrowdPlatformManager crowdPlatformManager, URL baseURL) {
        super(create);
        this.crowdPlatformManager = crowdPlatformManager;
        this.baseURL = baseURL;
        experimentDao = new ExperimentDao(create.configuration());
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
                .map(type -> new Hit(record, type, tags, type.getAmount(amount, ratingToAnswer), payment, 24*60*60, 30*24*60*60,
                        getExternalURL(inserted, type), type.getPlatform(platformAnswer, platformRating)));


        List<CompletableFuture<Hit>> publishingHits = create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
                .fetchOptional()
                .map(expRecordToHit::apply)
                .orElseThrow(() -> new ResourceNotFoundExcpetion("ExperimentTable " + expID + " not found"))
                .map(hit -> crowdPlatformManager.publishHit(hit, (hitR, ex) -> storeOrDelete(inserted, hit, ex)))
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

    public Response getRunning(Request request, Response response) {
        return createJson(request, response, () -> {
            List<JSONHit> map = create.select()
                    .from(Tables.HIT)
                    .join(Tables.EXPERIMENT).onKey()
                    .where(Tables.HIT.RUNNING.eq(true))
                    .fetch()
                    .map(record -> new JSONHit(record.getValue(Tables.EXPERIMENT.TITEL), record.into(Tables.HIT)));
            return gson.toJson(map);
        });
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

    public Response updateHIT(Request request, Response response) {
        return processJson(request, response, raw -> {
            JSONHit jsonHit = gson.fromJson(raw, JSONHit.class);
            HitRecord old = create.selectFrom(Tables.HIT)
                    .where(Tables.HIT.IDHIT.eq(jsonHit.getIdHit()))
                    .fetchOptional()
                    .orElseThrow(() -> new ResourceNotFoundExcpetion("hit " + jsonHit.getIdHit() + " not found."));

            HitRecord newRecord = create.update(Tables.HIT)
                    .set(jsonHit.getRecord())
                    .returning()
                    .fetchOptional()
                    .orElseThrow(() -> new InternalServerErrorException("hit " + jsonHit.getIdHit() + " could not be updated"));

            response.status(200);
            response.type("text/plain");
            response.body("success");
            if ((!Objects.equals(newRecord.getPayment(), old.getPayment()))) {
                ExperimentRecord experimentRecord = create.selectFrom(Tables.EXPERIMENT)
                        .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(newRecord.getExperimentH()))
                        .fetchOne();

                Result<TagsRecord> tags = create.selectFrom(Tables.TAGS)
                        .where(Tables.TAGS.EXPERIMENT_T.eq(newRecord.getExperimentH()))
                        .fetch();

                Hit update = new Hit(experimentRecord, newRecord, tags);
                crowdPlatformManager.updateHit(update)
                        .exceptionally(fail -> {
                            fail.printStackTrace();
                            response.body("error: unable to update hit");
                            return null;
                        }).join();
            }
        });
    }
}
