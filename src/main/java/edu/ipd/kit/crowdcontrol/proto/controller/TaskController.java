package edu.ipd.kit.crowdcontrol.proto.controller;

import edu.ipd.kit.crowdcontrol.proto.crowdplatform.CrowdPlatformManager;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.Tables;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.AnswersDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.ExperimentDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.HitDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Answers;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.AnswersRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.RatingsRecord;
import edu.ipd.kit.crowdcontrol.proto.web.CreativeTaskView;
import edu.ipd.kit.crowdcontrol.proto.web.RatingTaskView;
import edu.ipd.kit.crowdcontrol.proto.web.TaskView;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Webview of tasks
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @author skorz
 * @version 1.0
 */
//TODO: if a rating worker does not finish it will clutter the DB.
public class TaskController extends Controller {
    private final HitDao hitDAO;
    private final ExperimentDao expDAO;
    private final AnswersDao answersDao;
    private final CrowdPlatformManager crowdPlatformManager;

    public TaskController(DSLContext create, CrowdPlatformManager crowdPlatformManager) {
        super(create);
        this.crowdPlatformManager = crowdPlatformManager;
        hitDAO = new HitDao(create.configuration());
        expDAO = new ExperimentDao(create.configuration());
        answersDao = new AnswersDao(create.configuration());
    }

    public ModelAndView processRenderCreativeRequest(Request request, Response response) {
        return renderTask(request, response, (hit, exp) -> {
            String workerID = assertParameter(request, "workerID");
            return getCreativeTaskView(exp, workerID, hit);
        });
    }

    public ModelAndView processRenderRatingRequest(Request request, Response response) {
        return renderTask(request, response, (hit, exp) -> {
            String workerID = assertParameter(request, "workerID");
            return getRatingTaskView(exp, workerID, hit);
        });
    }

    public ModelAndView submitAnswerTask(Request request, Response response) {
        return renderTask(request, response, (hit, exp) -> {
            String workerID = assertParameter(request, "workID");
            String[] answersParam = request.raw().getParameterMap().get("answer");
            if (answersParam == null || answersParam.length == 0)
                throw new BadRequestException("need parameter answer with the answer");
            String answer = answersParam[0];
            AnswersRecord record =  new AnswersRecord(null, hit.getIdhit(), answer, null, workerID);
            int i = create.executeInsert(record);
            if (i == 0) {
                System.err.println("unable to insert AnswersRecord: " + answer);
                return Optional.empty();
            }
            return getCreativeTaskView(exp, workerID, hit);
        });
    }

    public ModelAndView submitRatingTask(Request request, Response response) {
        return renderTask(request, response, (hit, exp) -> {
            String workerID = assertParameter(request, "workID");
            String[] ratingsParam = request.raw().getParameterMap().get("ratingtext");
            if (ratingsParam == null || ratingsParam.length == 0)
                throw new BadRequestException("need parameter ratingtext with the rating");
            String[] idParam = request.raw().getParameterMap().get("ratingansid");
            if (idParam == null || idParam.length == 0)
                throw new BadRequestException("need parameter idParam with the ids");
            List<RatingsRecord> toInsert = IntStream.range(0, ratingsParam.length)
                    .filter(index -> !ratingsParam[index].isEmpty())
                    .mapToObj(index -> new RatingsRecord(null, hit.getIdhit(),
                            Integer.parseInt(idParam[index]), null, Integer.parseInt(ratingsParam[index]), workerID))
                    .collect(Collectors.toList());
            List<Integer> answerIDsToDelete = toInsert.stream()
                    .map(RatingsRecord::getAnswerR)
                    .collect(Collectors.toList());
            create.transaction(configuration -> {
                int execute = DSL.using(configuration)
                        .deleteFrom(Tables.RATINGS)
                        .where(Tables.RATINGS.WORKERID.eq(workerID))
                        .and(Tables.RATINGS.HIT_R.eq(hit.getIdhit()))
                        .and(Tables.RATINGS.ANSWER_R.in(answerIDsToDelete))
                        .execute();

                if (execute != toInsert.size()) {
                    System.err.println("not all Ratings deleted, toInsert: " + toInsert);
                }

                DSL.using(configuration)
                        .batchInsert(toInsert);
            });
            Optional<RatingTaskView> ratingTaskView = getRatingTaskView(exp, workerID, hit);
            if (!ratingTaskView.isPresent()) {
                //TODO: pay bonus?!
            }
            return ratingTaskView;
        });
    }

    private Optional<RatingTaskView> getRatingTaskView(Experiment experiment, String wokerID, Hit hit) {
        SelectConditionStep<RatingsRecord> select = DSL.selectFrom(Tables.RATINGS)
                .where(Tables.RATINGS.HIT_R.eq(hit.getIdhit()))
                .and(Tables.RATINGS.RATING.isNotNull())
                .and(Tables.RATINGS.WORKERID.eq(wokerID));
        int ratingsGiven = create.fetchCount(select);
        int amountToRate = experiment.getMaxRatingsPerAssignment() - ratingsGiven;
        if (amountToRate > 1) {
            List<Answers> answersToRate = create.transactionResult(config -> {
                Field<Integer> count = DSL.selectCount().from(Tables.RATINGS).asField();
                //Connect with max in CrowdPlatform
                LocalDateTime limit = LocalDateTime.now().minus(2, ChronoUnit.HOURS);
                Timestamp timestamp = Timestamp.valueOf(limit);
                List<Answers> toRate = DSL.using(config).select()
                        .select(Tables.ANSWERS.fields())
                        .select(count)
                        .from(Tables.ANSWERS)
                        .leftJoin(Tables.RATINGS).onKey()
                        .where(Tables.ANSWERS.HIT_A.eq(hit.getIdhit()))
                        .and(Tables.RATINGS.RATING.isNotNull().or(Tables.RATINGS.TIMESTAMP.greaterThan(timestamp)))
                        .having(count.lessThan(experiment.getMaxRatingsPerAssignment()))
                        .fetch()
                        .map(record -> record.into(Tables.ANSWERS))
                        .stream()
                        .limit(amountToRate)
                        .map(answersDao.mapper()::map)
                        .collect(Collectors.toList());

                List<RatingsRecord> emptyRatings = toRate.stream()
                        .map(answers -> new RatingsRecord(null, hit.getIdhit(), answers.getIdanswers(), null, null, wokerID))
                        .collect(Collectors.toList());

                DSL.using(config).batchInsert(emptyRatings);

                return toRate;
            });
            return Optional.of(new RatingTaskView(experiment, answersToRate, wokerID));
        } else {
            return Optional.empty();
        }
    }

    private Optional<CreativeTaskView> getCreativeTaskView(Experiment experiment, String wokerID, Hit hit) {
        SelectConditionStep<AnswersRecord> select = DSL.selectFrom(Tables.ANSWERS)
                .where(Tables.ANSWERS.WORKERID.eq(wokerID))
                .and(Tables.ANSWERS.HIT_A.eq(hit.getIdhit()));
        int answersGiven = create.fetchCount(select);
        int amountToAnswer = experiment.getMaxAnswersPerAssignment() - answersGiven;
        if (amountToAnswer > 1) {
            return Optional.of(new CreativeTaskView(experiment, true, wokerID));
        } else if (amountToAnswer == 1) {
            return Optional.of(new CreativeTaskView(experiment, false, wokerID));
        } else {
            return Optional.empty();
        }
    }

    private ModelAndView renderTask(Request request, Response response, BiFunction<Hit, Experiment, Optional<? extends TaskView>> func) {
        int hitID = assertParameterInt(request, "hitID");
        if (!hitDAO.existsById(hitID)) {
            //replace with right exception after merge
            throw new RuntimeException();
        }
        Hit hit = hitDAO.fetchOneByIdhit(hitID);
        Experiment experiment = expDAO.fetchOneByIdexperiment(hit.getExperimentH());
        return func.apply(hit, experiment)
                .map(TaskView::render)
                .orElseGet(() -> {
                    String mturk_url = "https://workersandbox.mturk.com/mturk/externalSubmit?assignmentId="
                            + hit.getIdCrowdPlatform() + "&foo=bar";
                    response.redirect(mturk_url);
                    response.body("Thank you");
                    return new ModelAndView(new HashMap<String, String>(), "thankYou.ftl");
                });
    }

    private Response submitTask(Request request, Response response, BiFunction<Hit, String, Response> func) {
        int hitID = assertParameterInt(request, "hitID");
        String workerID = assertParameter(request, "workerID");
        if (!hitDAO.existsById(hitID)) {
            //replace with right exception after merge
            throw new RuntimeException();
        }
        response.status(200);
        Hit hit = hitDAO.fetchOneByIdhit(hitID);
        return func.apply(hit, workerID);
    }
}
