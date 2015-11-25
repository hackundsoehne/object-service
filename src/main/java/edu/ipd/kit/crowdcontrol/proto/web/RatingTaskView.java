package edu.ipd.kit.crowdcontrol.proto.web;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Answers;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Experiment;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by skorz on 21.11.15.
 */
public class RatingTaskView implements TaskView {
    private final Experiment experiment;
    private final List<Answers> answers;
    private final String workerID;

    public RatingTaskView(Experiment experiment, List<Answers> answers, String workerID) {
        this.experiment = experiment;
        this.answers = answers;
        this.workerID = workerID;
    }

    @Override
    public ModelAndView render() {
        //TODO: its really ugly, cryptic API and not really usable, only copied from CreativeCrowd
        String view = "";
        // wat
        // TODO
        // must be submitted via javascript to make a difference between
        // different buttons pressed
        view += "<table>";
        Function<Answers, String> answer2TableData = answers -> {
            String temp = "";
            temp += "<tr><td class='answered'>" + answers.getAnswer()
                    + "</td><td><div id='ratingoptionsandtext'>"
                    + "<input type='text' name='rating"
                    + "text' placeholder='Comment this answer here'>"
                    + "<input type='hidden' name='rating"
                    + "ansid' value='" + answers.getIdanswers() + "'></div>";
            temp += "</div></td></tr>";
            return temp;
        };
        view += answers.stream().map(answer2TableData::apply).collect(Collectors.joining());
        view += "</div></td></tr>";
        view += "</table>";

        // Create the root hash
        Map<String, String> root = new HashMap<>();
        // Put string ``user'' into the root
        root.put("task", experiment.getQuestion());
        String pictureUrl = experiment.getPictureUrl();
        if (pictureUrl == null)
            pictureUrl = "";
        root.put("pic", pictureUrl);
        root.put("expId", "1356");
        root.put("workID", workerID);
        root.put("ratingTable", view);
        root.put("desc", experiment.getRatingDescription());
        root.put("exdesc", "");
        String licenseUrl = experiment.getPictureLicenseUrl();
        if (licenseUrl != null) {
            root.put("iframe", licenseUrl);
        } else {
            root.put("iframe", "");
        }
        root.put("again", "");
        root.put("next", "");
        // submit button always exists
        root.put("sub", "true");

        return new ModelAndView(root, "ratingTask.ftl");
    }
}
