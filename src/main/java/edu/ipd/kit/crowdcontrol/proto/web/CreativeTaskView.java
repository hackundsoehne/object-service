package edu.ipd.kit.crowdcontrol.proto.web;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skorz on 21.11.15.
 */
public class CreativeTaskView implements TaskView {
    private final Hit task;
    private final Experiment experiment;

    public CreativeTaskView(Hit task, Experiment experiment) {
        this.task = task;
        this.experiment = experiment;
    }

    public static ModelAndView doRender(Hit task, Experiment experiment) {
        return new CreativeTaskView(task, experiment).render();
    }


    @Override
    public ModelAndView render() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("task", experiment.getQuestion());
        attributes.put("pic", experiment.getPictureUrl());
        attributes.put("exdesc", experiment.getDescription());
        attributes.put("expid", experiment.getIdexperiment());
        //WTF! Warum haben die in cc das iframe genannt?
        attributes.put("iframe", experiment.getPictureLicenseUrl());
        attributes.put("next", "");
        attributes.put("again", "");
        attributes.put("sub", "");

        return new ModelAndView(attributes, "creativeTask.ftl");
    }
}
