package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.Streams;
import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.json.JSONHitAnswer;
import javafx.scene.control.Tab;
import org.jooq.DSLContext;
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
    private final CrowdPlatformManager crowdPlatformManager;

    public CrowdComputingController(DSLContext create, CrowdPlatformManager crowdPlatformManager) {
        this.create = create;
        this.crowdPlatformManager = crowdPlatformManager;
    }

    public Response startExperiment(Request request, Response response) {
        int expID = assertParameterInt(request, "expID");
        int payment = assertParameterInt(request, "payment");
        int amount = assertParameterInt(request, "amount");
        String platformWorker = assertParameter(request, "platformWorker");
        String platformRating = assertParameter(request, "platformRating");
        int workerToRating = assertParameterInt(request, "workerToRating");
        create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.IDEXPERIMENT.eq(expID))
                .fetchOne()
                .map(record -> new Hit())
        crowdPlatformManager.getCrowdplatform(platformWorker)
                .orElseThrow(() -> new BadRequestException("error platform not found: " + platformWorker))
                .
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
