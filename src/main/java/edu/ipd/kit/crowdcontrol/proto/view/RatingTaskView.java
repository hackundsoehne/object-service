package edu.ipd.kit.crowdcontrol.proto.view;

import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skorz on 21.11.15.
 */
public class RatingTaskView implements TaskView {
    @Override
    public ModelAndView render() {
        // TODO implement
        Map<String, Object> attributes = new HashMap<>();

        return new ModelAndView(attributes, "ratingTask.ftl");
    }
}
