package edu.ipd.kit.crowdcontrol.proto.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.AnswersDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.ExperimentDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.HitDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.RatingsDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Answers;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Ratings;
import edu.ipd.kit.crowdcontrol.proto.json.JSONRating;
import edu.ipd.kit.crowdcontrol.proto.view.CreativeTaskView;
import edu.ipd.kit.crowdcontrol.proto.view.TaskView;
import org.jooq.DSLContext;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.function.BiFunction;

/**
 * Webview of tasks
 * Created by skorz on 20.11.15.
 */
public class TaskController implements ControllerHelper {
    private final DSLContext create;
    private final HitDao hitDAO;
    private final ExperimentDao expDAO;
    private final AnswersDao answersDao;
    private final RatingsDao ratingsDao;
    private final Gson gson;

    public TaskController(DSLContext create) {
        this.create = create;
        hitDAO = new HitDao(create.configuration());
        expDAO = new ExperimentDao(create.configuration());
        answersDao = new AnswersDao(create.configuration());
        ratingsDao = new RatingsDao(create.configuration());
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    public ModelAndView renderAnswerTask(Request request, Response response) {
        return renderTask(request, response, CreativeTaskView::new);
    }

    public Response submitAnswerTask(Request request, Response response) {
        return submitTask(request, response, (hit, workerID) -> {
            Answers answers = new Answers(null, hit.getIdhit(), request.body(), null, workerID);
            answersDao.insert(answers);
            response.body("success");
            return response;
        });
    }

    public Response submitRatingTask(Request request, Response response) {
        return submitTask(request, response, (hit, workerID) -> {
            assertJson(request);
            JSONRating jsonRating = gson.fromJson(request.body(), JSONRating.class);
            Ratings ratings = new Ratings(null, hit.getIdhit(), jsonRating.getAnswerID(), null, jsonRating.getRating(), workerID);
            ratingsDao.insert(ratings);
        });
    }

    private ModelAndView renderTask(Request request, Response response, BiFunction<Hit, Experiment, TaskView> func) {
        int hitID = assertParameterInt(request, "hitID");
        if (!hitDAO.existsById(hitID)) {
            //replace with right exception after merge
            throw new RuntimeException();
        }
        Hit hit = hitDAO.fetchOneByIdhit(hitID);
        Experiment experiment = expDAO.fetchOneByIdexperiment(hit.getExperimentH());
        TaskView taskView = func.apply(hit, experiment);
        return taskView.render();
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
