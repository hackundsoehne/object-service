package playground;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Answers;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import edu.ipd.kit.crowdcontrol.proto.web.FreeMarkerEngine;
import edu.ipd.kit.crowdcontrol.proto.web.RatingTaskView;
import spark.ModelAndView;
import spark.servlet.SparkApplication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * @author LeanderK
 * @version 1.0
 */
public class RouterRatingWebTest implements SparkApplication {
    /**
     * Invoked from the SparkFilter. Add routes here.
     */
    @Override
    public void init() {
        staticFileLocation("/public");
        FreeMarkerEngine engine = new FreeMarkerEngine();
        get("/tasks/rating/:exID", ((request, response) -> {
            Experiment experiment = new Experiment(1234,
                    "https://upload.wikimedia.org/wikipedia/commons/5/52/Jesus-Christ-from-Hagia-Sophia.jpg",
                    "https://commons.wikimedia.org/wiki/File:Jesus-Christ-from-Hagia-Sophia.jpg",
                    "trololololo",
                    "CC is ingnoring this somehow?",
                    "titelVariable",
                    3,3,
                    "answerDescripton",
                    "raingDescription");
            Answers answers = new Answers(1, 1, "wow! so great!", null, "1234");
            Answers answers2 = new Answers(2, 1, "much beautiful", null, "1235");
            RatingTaskView ratingTaskView = new RatingTaskView(experiment, Arrays.asList(answers, answers2), "13");
            return ratingTaskView.render();
        }), engine);

        post("/tasks/rating/:exID/:workID", ((request, response) -> {
            String view = "";
            // wat
            // TODO
            // must be submitted via javascript to make a difference between
            // different buttons pressed
            view += "";
            view += "<table>";
            view += "<tr><td class='answered'>" + "Was steht hier 1?"
                    + "</td><td><div id='ratingoptionsandtext'>"; /*-?|Simon|simon|c0|?*/
            view += "Und hier 1?"
                    + "<input type='text' name='rating" + 1
                    + "text' placeholder='Comment this answer here'>"
                    + "<input type='hidden' name='rating" + 1
                    + "ansid' value='" + "11111" + "'></div>";
            view += "</div></td></tr>";
            view += "<tr><td class='answered'>" + "Was steht hier 2?"
                    + "</td><td><div id='ratingoptionsandtext'>"; /*-?|Simon|simon|c0|?*/
            view += "Und hier 2?"
                    + "<input type='text' name='rating" + 2
                    + "text' placeholder='Comment this answer here'>"
                    + "<input type='hidden' name='rating" + 2
                    + "ansid' value='" + "222222" + "'></div>";
            view += "</div></td></tr>";
            view += "</table>";

            // Create the root hash
            Map<String, String> root = new HashMap<String, String>();
            // Put string ``user'' into the root
            root.put("task", "trolololol");
            root.put("pic", "https://upload.wikimedia.org/wikipedia/commons/5/52/Jesus-Christ-from-Hagia-Sophia.jpg");
            root.put("expId", "1356");
            root.put("ratingTable", view);
            root.put("desc", "you rate!");
            String exdesc = "so decription....twice?";
            if(exdesc!=null){
                root.put("exdesc", exdesc);
            }else{
                root.put("exdesc", "");
            }
            String licenseUrl = "https://commons.wikimedia.org/wiki/File:Jesus-Christ-from-Hagia-Sophia.jpg";
            if (licenseUrl != null) {

                root.put("iframe", licenseUrl); /*-?|Test Repo-Review|simon|c0|?*/
            } else {
                root.put("iframe", "");
            }
            // set if the buttons appear
            root.put("again", "true");
            root.put("next", "true");
            // submit button always exists
            root.put("sub", "true");

            root.put("submitaction", "somemturkurl");
            root.put("againaction", "localhost"
                    + "/rating/" + "1344" + "?assignmentId=" + "1345");
            // TODO: Macht das hier Sinn?
            root.put("nextaction", "http://localhost:4567"
                    + "/rating/" + "41141" + "?assignmentId=" + "131441");

            return new ModelAndView(root, "ratingTask.ftl");
        }), engine);

        post("/tasks/rating/:expID", ((request, response) -> {
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

        get("/tasks/rating/:exID", ((request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("task", "hagia sophia!");
            attributes.put("pic", "https://upload.wikimedia.org/wikipedia/commons/5/52/Jesus-Christ-from-Hagia-Sophia.jpg");
            attributes.put("exdesc", "do something funny");
            attributes.put("expid", "12155236");
            //WTF! Warum haben die in cc das iframe genannt?
            attributes.put("iframe", "https://commons.wikimedia.org/wiki/File:Jesus-Christ-from-Hagia-Sophia.jpg");
            attributes.put("next", "n");
            attributes.put("again", "");
            attributes.put("sub", "s");

            return new ModelAndView(attributes, "creativeTaskPreview.ftl");
        }), engine);
    }

    public static void main(String[] args) {
        new RouterRatingWebTest().init();
    }
}
