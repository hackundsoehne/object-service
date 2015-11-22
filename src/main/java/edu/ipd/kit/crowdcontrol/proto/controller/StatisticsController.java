package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.AnswersDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.HitDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Answers;
import edu.ipd.kit.crowdcontrol.proto.json.JSONFullTask;
import edu.ipd.kit.crowdcontrol.proto.json.JSONHitOverview;
import edu.ipd.kit.crowdcontrol.proto.json.JSONSimpelTask;
import org.jooq.DSLContext;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * this controller is responsible for all rest-calls regarding statistics.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class StatisticsController implements ControllerHelper {
    private final DSLContext create;
    private final AnswersDao answersDao;
    private final HitDao hitDao;
    private final Gson gson;

    public StatisticsController(DSLContext create) {
        this.create = create;
        gson = new GsonBuilder().create();
        answersDao = new AnswersDao(create.configuration());
        hitDao = new HitDao(create.configuration());
    }

    public Response getAnswersCSV(Request request, Response response) {
        return getStatistics(request, response, "text/csv", hit -> {
            return create.selectFrom(Tables.ANSWERS)
                    .where(Tables.ANSWERS.HIT_A.eq(hit))
                    .fetch()
                    .formatCSV(';');
        });
    }

    public Response getRatingsCSV(Request request, Response response) {
        return getStatistics(request, response, "text/csv", hit -> {
            return create.selectFrom(Tables.RATINGS)
                    .where(Tables.RATINGS.HIT_R.eq(hit))
                    .fetch()
                    .formatCSV(';');
        });
    }

    public Response getCSV(Request request, Response response) {
        return getStatistics(request, response, "text/csv", hit -> {
            Hit hit_answer = Tables.HIT.as("hit_answer");
            Hit hit_rating = Tables.HIT.as("hit_rating");
            return create.select(Tables.ANSWERS.IDANSWERS, Tables.ANSWERS.ANSWER,
                                Tables.ANSWERS.HIT_A.as("answer_hit_id"), Tables.ANSWERS.TIMESTAMP.as("answer_timestamp"),
                                hit_answer.BONUS.as("answer_bonus"), hit_answer.PAYMENT.as("answer_payment"),
                                hit_answer.CROWD_PLATFORM.as("answer_platform"),
                                Tables.RATINGS.RATING, Tables.RATINGS.TIMESTAMP.as("rating_timestamp"), Tables.RATINGS.IDRATINGS,
                                Tables.RATINGS.HIT_R.as("rating_hit"),
                                hit_rating.PAYMENT.as("rating_payment"), hit_rating.CROWD_PLATFORM.as("rating_platform"),
                                Tables.EXPERIMENT.MAX_ANSWERS_PER_ASSIGNMENT, Tables.EXPERIMENT.MAX_RATINGS_PER_ASSIGNMENT,
                                Tables.EXPERIMENT.PICTURE_URL, Tables.EXPERIMENT.QUESTION, Tables.EXPERIMENT.TITEL)
                    .from(Tables.ANSWERS)
                    .join(hit_answer).onKey()
                    .leftJoin(Tables.RATINGS).onKey()
                    .join(hit_rating).on(Tables.RATINGS.HIT_R.eq(hit_rating.IDHIT))
                    .join(Tables.EXPERIMENT).on(hit_answer.EXPERIMENT_H.eq(Tables.EXPERIMENT.IDEXPERIMENT))
                    .fetch()
                    .formatCSV(';');
        });
    }

    public Response getSimpleTasksJSON(Request request, Response response) {
        int hit = assertParameterInt(request, "hit");
        String timestampRaw = request.params("limit");
        Timestamp timestamp = null;
        if (timestampRaw != null) {
            try {
                timestamp =  Timestamp.valueOf(timestampRaw);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("parameter limit does not have the format yyyy-[m]m-[d]d hh:mm:ss");
            }
        } else {
            timestamp = new Timestamp(0);
        }
        List<JSONSimpelTask> answers = create.selectFrom(Tables.ANSWERS)
                .where(Tables.ANSWERS.TIMESTAMP.greaterOrEqual(timestamp))
                .and(Tables.ANSWERS.HIT_A.eq(hit))
                .fetch()
                .map(JSONSimpelTask::new);

        List<JSONSimpelTask> results = create.selectFrom(Tables.RATINGS)
                .where(Tables.RATINGS.TIMESTAMP.greaterOrEqual(timestamp))
                .and(Tables.RATINGS.HIT_R.eq(hit))
                .fetch()
                .map(JSONSimpelTask::new);

        String json = gson.toJson(answers.addAll(results));
        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

    public Response getFullTaskJSON(Request request, Response response) {
        int id = assertParameterInt(request, "id");
        String type = assertParameter(request, "type");
        String json = null;
        //TODO: replace with enum after merge with database branch
        //TODO: replace after merge with right exception
        if (type.equals("ANSWERS")) {
            Answers answers = answersDao.fetchOptional(Tables.ANSWERS.IDANSWERS, id)
                    .orElseThrow(() -> new RuntimeException("answer " + id + " not found"));
            json = hitDao.fetchOptional(Tables.HIT.IDHIT, answers.getHitA())
                    .map(hit -> new JSONFullTask(hit, answers))
                    .map(gson::toJson)
                    .orElseThrow(() -> new RuntimeException("hit " + answers.getHitA() + " not found"));
        } else if (type.equals("QUESTIONS")) {
            Answers answers = answersDao.fetchOptional(Tables.ANSWERS.IDANSWERS, id)
                    .orElseThrow(() -> new RuntimeException("answer " + id + " not found"));
            json = hitDao.fetchOptional(Tables.HIT.IDHIT, answers.getHitA())
                    .map(hit -> new JSONFullTask(hit, answers))
                    .map(gson::toJson)
                    .orElseThrow(() -> new RuntimeException("hit " + answers.getHitA() + " not found"));
        } else {
            throw new RuntimeException("resource not found: type="+type);
        }
        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

    public Response getAllHitsOverview(Request request, Response response) {
        List<JSONHitOverview> hits = create.selectFrom(Tables.HIT)
                .fetch()
                .map(hitDao.mapper())
                .stream()
                .map(JSONHitOverview::new)
                .collect(Collectors.toList());

        String json = gson.toJson(hits);

        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

    public Response getHit(Request request, Response response) {
        int hitID = assertParameterInt(request, "hit");
        //TODO: replace after merge with database right exception, JSONHit Class
        String json = hitDao.fetchOptional(Tables.HIT.IDHIT, hitID)
                //a map should occur here, into JSONHitAnswer
                .map(Function.identity())
                .map(gson::toJson)
                .orElseThrow(() -> new RuntimeException("hit " + hitID + " not found"));

        response.status(200);
        response.body(json);
        response.type("application/json");
        return response;
    }

    public Response getStatistics(Request request, Response response, String contentType, Function<Integer, String> function) {
        int hit = assertParameterInt(request, "hit");
        response.status(200);
        response.type(contentType);
        response.header("Content-Disposition", "attachment");
        String body = function.apply(hit);
        response.body(body);
        return response;
    }
}
