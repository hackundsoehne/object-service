package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.jooq.Result;

import javax.mail.MessagingException;
import java.io.*;
import java.util.*;
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
    private WorkerOperations workerOps;


    public FeedbackSender(MailHandler handler, AnswerRatingOperations answerOps, ExperimentOperations expOps, WorkerOperations workerOps) {
        this.handler = handler;
        this.answerOps = answerOps;
        this.expOps = expOps;
        this.workerOps = workerOps;
    }

    public void sendFeedback(int expId) throws FeedbackException {
        String feedbackMessage = loadMessage("src/main/resources/feedbackMessage.txt");
        String feedbackAnswer = loadMessage("src/main/resources/feedbackAnswer.txt");
        String feedbackRating = loadMessage("src/main/resources/feedbackRating.txt");

        Result<AnswerRecord> answers = answerOps.getAnswersOfExperiment(expId);

        Collections.sort(answers, new workerComparator());

        StringBuilder answerMessage = new StringBuilder();

        Map<String, String> map = new HashMap<>();
        map.put("answers", answerMessage.toString());
        map.put("experimentName", expOps.getExperiment(expId).get().getTitle());

        int prevWorkerId = -1;
        for (AnswerRecord answer : answers) {
            if (prevWorkerId != answer.getWorkerId() && prevWorkerId != -1) {
                String workerMessage = Template.apply(feedbackMessage, map);
                if (workerOps.getWorker(prevWorkerId).isPresent()) {
                    try {
                        handler.sendMail(workerOps.getWorker(prevWorkerId).get().getEmail(), "Feedback to your work", workerMessage);
                    } catch (MessagingException e) {
                        throw new FeedbackException("The mailHandler cannot send mails. It seems, that there is a problem with the mailserver or the properties file.");
                    } catch (UnsupportedEncodingException e) {
                        throw new FeedbackException("The mailHandler cannot send mails to a worker, because the mail address of the worker is invalid.");
                    }
                } else {
                    throw new FeedbackException("The worker, who gave a answer cannot be found in the database.");
                }
                answerMessage = new StringBuilder();
            }
            List<RatingRecord> ratings = answerOps.getRatingsOfAnswer(answer);

            StringBuilder ratingMessage = new StringBuilder();

            for (RatingRecord rating : ratings) {
                Map<String, String> ratingMap = new HashMap<>();
                map.put("feedback", rating.getFeedback());
                map.put("quality", rating.getQuality().toString());
                map.put("rating", rating.getRating().toString());

                ratingMessage.append(Template.apply(feedbackRating, ratingMap));
            }

            Map<String, String> answerMap = new HashMap<>();
            map.put("answer", answer.getAnswer());
            map.put("ratings", ratingMessage.toString());

            answerMessage.append(Template.apply(feedbackAnswer, answerMap));

            prevWorkerId = answer.getWorkerId();
        }


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

    private class workerComparator implements Comparator<AnswerRecord> {

        @Override
        public int compare(AnswerRecord answerRecord, AnswerRecord t1) {
            if (answerRecord.getWorkerId() < t1.getWorkerId()) {
                return -1;
            } else if (answerRecord.getWorkerId() > t1.getWorkerId()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
