package playground;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.web.CreativeTaskView;
import edu.ipd.kit.crowdcontrol.proto.web.FreeMarkerEngine;
import spark.ModelAndView;
import spark.servlet.SparkApplication;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

/**
 * @author LeanderK
 * @version 1.0
 */
public class RouterAnswerWebTest implements SparkApplication {
    /**
     * Invoked from the SparkFilter. Add routes here.
     */
    @Override
    public void init() {
        staticFileLocation("/public");
        FreeMarkerEngine engine = new FreeMarkerEngine();
        get("/tasks/answer/:exID", ((request, response) -> {
            Experiment experiment = new Experiment(1234,
                    "https://upload.wikimedia.org/wikipedia/commons/5/52/Jesus-Christ-from-Hagia-Sophia.jpg",
                    "https://commons.wikimedia.org/wiki/File:Jesus-Christ-from-Hagia-Sophia.jpg",
                    "trololololo",
                    "CC is ingnoring this somehow?",
                    "titelVariable",
                    3,3,
                    "answerDescripton",
                    "raingDescription");

            CreativeTaskView creativeTaskView = new CreativeTaskView(experiment, true, "test!");
            return creativeTaskView.render();
        }), engine);

        post("/tasks/answer/:expID", ((request, response) -> {
            Map<String, String[]> parameterMap = request.raw().getParameterMap();
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("task", "hagia sophia!");
            attributes.put("pic", "https://upload.wikimedia.org/wikipedia/commons/5/52/Jesus-Christ-from-Hagia-Sophia.jpg");
            attributes.put("exdesc", "do something funny");
            attributes.put("expid", "12155236");
            //WTF! Warum haben die in cc das iframe genannt?
            attributes.put("iframe", "https://commons.wikimedia.org/wiki/File:Jesus-Christ-from-Hagia-Sophia.jpg");
            attributes.put("next", "n");
            attributes.put("again", "a");
            attributes.put("sub", "s");

            return new ModelAndView(attributes, "creativeTask.ftl");
        }), engine);

        post("/tasks/answer/:expID/submit", ((request, response) -> {
            Map<String, String> params = request.params();
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("task", "hagia sophia!");
            attributes.put("pic", "https://upload.wikimedia.org/wikipedia/commons/5/52/Jesus-Christ-from-Hagia-Sophia.jpg");
            attributes.put("exdesc", "do something funny");
            attributes.put("expid", "12155236");
            //WTF! Warum haben die in cc das iframe genannt?
            attributes.put("iframe", "https://commons.wikimedia.org/wiki/File:Jesus-Christ-from-Hagia-Sophia.jpg");
            attributes.put("next", "n");
            attributes.put("again", "a");
            attributes.put("sub", "s");

            return new ModelAndView(attributes, "creativeTask.ftl");
        }), engine);
    }

    public static void main(String[] args) {
        new RouterAnswerWebTest().init();
    }
}
