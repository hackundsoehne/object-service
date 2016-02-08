package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.jooq.Result;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Sends feedback to workers according to their answers.
 *
 * @author Felix Rittler
 */
public class FeedbackSender {

    private MailSender handler;
    private AnswerRatingOperations answerOps;
    private ExperimentOperations expOps;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> schedule = null;

    public FeedbackSender(MailHandler handler, AnswerRatingOperations answerOps, ExperimentOperations expOps) {
        this.handler = handler;
        this.answerOps = answerOps;
        this.expOps = expOps;
    }

    public void sendFeedback(int expId) throws FeedbackException {
        String feedbackMessage = loadMessage("src/main/resources/feedbackMessage.txt");
        String feedbackAnswer = loadMessage("src/main/resources/feedbackAnswer.txt");
        String feedbackRating = loadMessage("src/main/resources/feedbackRating.txt");

        //TODO: no message of all answers, but per worker

        Result<AnswerRecord> answers = answerOps.getAnswersOfExperiment(expId);

        StringBuilder answerMessage = new StringBuilder();

        for (AnswerRecord answer : answers) {
            List<RatingRecord> ratings = answerOps.getRatingsOfAnswer(answer);

            StringBuilder ratingMessage = new StringBuilder();

            for (RatingRecord rating : ratings) {
                Map<String, String> map = new HashMap<>();
                map.put("feedback", rating.getFeedback());
                map.put("quality", rating.getQuality().toString());
                map.put("rating", rating.getRating().toString());

                ratingMessage.append(Template.apply(feedbackRating, map));
            }

            Map<String, String> map = new HashMap<>();
            map.put("answer", answer.getAnswer());
            map.put("ratings", ratingMessage.toString());

            answerMessage.append(Template.apply(feedbackAnswer, map));
        }

        Map<String, String> map = new HashMap<>();
        map.put("answers", answerMessage.toString());
        map.put("experimentName", expOps.getExperiment(expId).get().getTitle());
        String workerMessage = Template.apply(feedbackMessage, map);

        //handler.sendMail();
    }

    private static String loadMessage(String path) throws FeedbackException {
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
            throw new FeedbackException("The file at \"" + path + "\" couldn't be found. Please secure, that there is a file.");
        } catch (IOException e) {
            throw new FeedbackException("The file at \"" + path + "\" couldn't be read. Please secure, that the file isn't corrupt");
        }
        return content.toString();
    }
}
