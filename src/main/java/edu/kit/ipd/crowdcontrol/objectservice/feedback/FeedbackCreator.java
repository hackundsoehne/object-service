package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    static String loadMessage(String path) throws FeedbackException {
        StringBuilder content = new StringBuilder();

        try {
            FileReader file = new FileReader(path);
            BufferedReader reader = new BufferedReader(file);
            String messageLine;
            while ((messageLine = reader.readLine()) != null) {
                content.append(messageLine);
                content.append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException e) {
            throw new FeedbackException("Sending of Feedback failed: The file at \"" + path + "\" couldn't be found. Please secure, that there is a file.");
        } catch (IOException e) {
            throw new FeedbackException("Sending of Feedback failed: The file at \"" + path + "\" couldn't be read. Please secure, that the file isn't corrupt");
        }
        return content.toString();
    }

    public String getFeedback(int expId, int workerId) throws FeedbackException {

        LOGGER.trace("Started creating feedback message to worker " + workerId + ".");

        String feedbackMessage = loadMessage("src/main/resources/feedback/feedbackMessage.txt");
        String feedbackAnswer = loadMessage("src/main/resources/feedback/feedbackAnswer.txt");
        String feedbackRating = loadMessage("src/main/resources/feedback/feedbackRating.txt");

        ExperimentRecord exp = expOps.getExperiment(expId).orElseThrow(() -> new FeedbackException("Experiment cannot be found."));

        List<AnswerRecord> answers = answerOps.getAnswersOfExperimentOfWorker(expId, workerId);

        StringBuilder answerMessage = new StringBuilder();

        Map<String, String> map = new HashMap<>();
        map.put("experimentName", exp.getTitle());

        //iterate over answers and send them and the feedback to the workers
        for (AnswerRecord answer : answers) {
            //List all ratings to an answer in the message.
            List<RatingRecord> ratings = answerOps.getRatingsOfAnswer(answer);

            StringBuilder ratingMessage = new StringBuilder();

            for (RatingRecord rating : ratings) {
                Map<String, String> ratingMap = new HashMap<>();
                ratingMap.put("feedback", rating.getFeedback());
                ratingMap.put("quality", rating.getQuality().toString());
                ratingMap.put("rating", rating.getRating().toString());

                ratingMessage.append(Template.apply(feedbackRating, ratingMap));
            }

            //Replace placeholders with answer and the ratings
            Map<String, String> answerMap = new HashMap<>();
            answerMap.put("answer", answer.getAnswer());
            answerMap.put("ratings", ratingMessage.toString());

            answerMessage.append(Template.apply(feedbackAnswer, answerMap));
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
