package edu.ipd.kit.crowdcontrol.proto.controller;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.ExperimentDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.HitDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import edu.ipd.kit.crowdcontrol.proto.view.CreativeTaskView;
import edu.ipd.kit.crowdcontrol.proto.view.RatingTaskView;
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

    public TaskController(DSLContext create) {
        this.create = create;
        hitDAO = new HitDao(create.configuration());
        expDAO = new ExperimentDao(create.configuration());
    }

    public ModelAndView renderAnswerTask(Request request, Response response) {
        return renderTask(request, response, CreativeTaskView::new);
    }

    public ModelAndView renderRatingTask(Request request, Response response) {
        return renderTask(request, response, RatingTaskView::new);
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
}
