package edu.ipd.kit.crowdcontrol.proto.view;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skorz on 21.11.15.
 */
public class CreativeTaskView implements TaskView {
    Hit task = null;
    Experiment experiment = null;

    public CreativeTaskView(Hit task, Experiment experiment) {
        this.task = task;
        this.experiment = experiment;
    }

    @Override
    public ModelAndView render() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("task", experiment.getQuestion());
        attributes.put("pic", "");
        attributes.put("exdesc", experiment.getDescription());
        attributes.put("expid", experiment.getIdexperiment());
        attributes.put("iframe", "");
        attributes.put("next", "");
        attributes.put("again", "");
        attributes.put("sub", "");

        return new ModelAndView(attributes, "creativeTask.ftl");
    }
}
