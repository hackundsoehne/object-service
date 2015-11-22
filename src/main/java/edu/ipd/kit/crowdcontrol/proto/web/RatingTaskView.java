package edu.ipd.kit.crowdcontrol.proto.web;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skorz on 21.11.15.
 */
public class RatingTaskView implements TaskView {
    private final Hit task ;
    private final Experiment experiment;

    public RatingTaskView(Hit task, Experiment experiment) {
        this.task = task;
        this.experiment = experiment;
    }

    @Override
    public ModelAndView render() {
        // TODO implement
        Map<String, Object> attributes = new HashMap<>();

        return new ModelAndView(attributes, "ratingTask.ftl");
    }
}
