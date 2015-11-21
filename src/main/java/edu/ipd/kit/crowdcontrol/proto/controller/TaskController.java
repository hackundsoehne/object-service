package edu.ipd.kit.crowdcontrol.proto.controller;

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

    public TaskController(DSLContext create) {
        this.create = create;
    }

    public ModelAndView renderTask(Request request, Response response) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("task", "The Task");
        attributes.put("pic", "");
        attributes.put("exdesc", "The experiment description");
        attributes.put("expid", "The experiment id");
        attributes.put("iframe", "");
        attributes.put("next", "");
        attributes.put("again", "");
        attributes.put("sub", "");

        return new ModelAndView(attributes, "creativetask.ftl");
    }
}
