package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends feedback to workers according to their answers.
 *
 * @author Felix Rittler
 */
public class FeedbackCreator {

    private static final Logger LOGGER = LogManager.getLogger(FeedbackCreator.class);
    private AnswerRatingOperations answerOps;
    private ExperimentOperations expOps;
    private WorkerOperations workerOps;

    public FeedbackCreator(AnswerRatingOperations answerOps, ExperimentOperations expOps, WorkerOperations workerOps) {
        this.answerOps = answerOps;
        this.expOps = expOps;
        this.workerOps = workerOps;
    }

    public String getFeedback(int expId, int workerId) throws FeedbackException {

        LOGGER.trace("Started creating feedback message to worker " + workerId + ".");

        String feedbackMessage = Utils.loadFile("/feedback/feedbackMessage.txt");
        String feedbackAnswer = Utils.loadFile("/feedback/feedbackAnswer.txt");
        String feedbackRating = Utils.loadFile("/feedback/feedbackRating.txt");

        ExperimentRecord exp = expOps.getExperiment(expId).orElseThrow(() -> new FeedbackException("Experiment cannot be found."));

        List<AnswerRecord> answers = answerOps.getAnswersOfWorkerFromExperiment(expId, workerId);

        StringBuilder answerMessage = new StringBuilder();

        Map<String, String> map = new HashMap<>();
        map.put("experimentName", exp.getTitle());

        //iterate over answers and send them and the feedback to the workers
        for (AnswerRecord answer : answers) {
            //List all ratings to an answer in the message.
            List<RatingRecord> ratings = answerOps.getRatingsOfAnswer(answer);

            StringBuilder ratingMessage = new StringBuilder();

            for (RatingRecord rating : ratings) {
                String feedback = rating.getFeedback();
                if (rating.getFeedback() == null || rating.getFeedback().equals("")) {
                    feedback = "Rater didn't give feedback";
                }
                Map<String, String> ratingMap = new HashMap<>();
                ratingMap.put("feedback", feedback);
                ratingMap.put("quality", rating.getQuality().toString());
                ratingMap.put("rating", rating.getRating().toString());

                ratingMessage.append(Template.apply(feedbackRating, ratingMap)).append(System.getProperty("line.separator"));
            }

            //Replace placeholders with answer and the ratings
            String systemFeedback;
            if (answer.getSystemResponse() == null || answer.getSystemResponse().equals("")) {
                systemFeedback = "";
            } else {
                systemFeedback = "Additional feedback: " + answer.getSystemResponse() + System.getProperty("line.separator");
            }

            Map<String, String> answerMap = new HashMap<>();
            answerMap.put("answer", answer.getAnswer());
            answerMap.put("ratings", ratingMessage.toString());
            answerMap.put("systemResponse", systemFeedback);

            answerMessage.append(Template.apply(feedbackAnswer, answerMap)).append(System.getProperty("line.separator"));
        }

        //Send feedback to the last worker
        String message = "";
        if (!answers.isEmpty()) {
            map.put("answers", answerMessage.toString());
            message = Template.apply(feedbackMessage, map);
        }
        LOGGER.trace("Completed creating feedback message to worker " + workerId + ".");
        return message;
    }
}
