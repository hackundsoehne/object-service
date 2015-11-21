package edu.ipd.kit.crowdcontrol.proto.controller;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.ExperimentDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.daos.HitDao;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord;
import edu.ipd.kit.crowdcontrol.proto.view.CreativeTaskView;
import edu.ipd.kit.crowdcontrol.proto.view.TaskView;
import org.jooq.DSLContext;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

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
        hitDAO = new HitDao();
        expDAO = new ExperimentDao();
    }

    public ModelAndView renderTask(Request request, Response response) {
        int hitID = assertParameterInt(request, "hitID");
        Hit hit = hitDAO.fetchOneByIdhit(hitID);
        // TODO check id exists
        Experiment experiment = expDAO.fetchOneByIdexperiment(hit.getExperimentH());
        // TODO creative task or rating task?
        TaskView taskView = new CreativeTaskView(hit, experiment);
        return taskView.render();
    }
}
