package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;

import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;

import edu.kit.ipd.crowdcontrol.objectservice.template.Template;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jooq.Result;

import javax.mail.MessagingException;

import java.io.*;
import java.util.*;

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

    private static final Logger LOGGER = LogManager.getLogger(FeedbackSender.class);

    public FeedbackSender(MailHandler handler, AnswerRatingOperations answerOps, ExperimentOperations expOps, WorkerOperations workerOps) {
        this.handler = handler;
        this.answerOps = answerOps;
        this.expOps = expOps;
        this.workerOps = workerOps;
    }

    public void sendFeedback(int expId) throws FeedbackException {

        LOGGER.trace("Started sending feedback to workers.");

        String feedbackMessage = loadMessage("src/main/resources/feedback/feedbackMessage.txt");
        String feedbackAnswer = loadMessage("src/main/resources/feedback/feedbackAnswer.txt");
        String feedbackRating = loadMessage("src/main/resources/feedback/feedbackRating.txt");

        Result<AnswerRecord> answers = answerOps.getAnswersOfExperiment(expId);

        //sort answers depending on the worker, who gave the answer
        Collections.sort(answers, (answerRecord, t1) -> {
            if (answerRecord.getWorkerId() < t1.getWorkerId()) {
                return -1;
            } else if (answerRecord.getWorkerId() > t1.getWorkerId()) {
                return 1;
            } else {
                return 0;
            }
        });

        StringBuilder answerMessage = new StringBuilder();

        Map<String, String> map = new HashMap<>();
        map.put("answers", feedbackAnswer);
        Optional<ExperimentRecord> exp = expOps.getExperiment(expId);
        if (exp.isPresent()) {
            map.put("experimentName", exp.get().getTitle());
        } else {
            LOGGER.error("Error, experiment cannot be found.");
            throw new FeedbackException("Experiment cannot be found.");
        }

        //iterate over answers and send them and the feedback to the workers
        int workerCount = 0;
        int prevWorkerId = -1;
        for (AnswerRecord answer : answers) {
            //checks, if the worker changed. If so, send the message to the worker and begin a new
            if (prevWorkerId != answer.getWorkerId() && prevWorkerId != -1) {
                workerCount++;
                String workerMessage = Template.apply(feedbackMessage, map);
                sendFeedback(prevWorkerId, workerMessage);
                answerMessage = new StringBuilder();
            }

            //List all ratings to an answer in the message.
            List<RatingRecord> ratings = answerOps.getRatingsOfAnswer(answer);

            StringBuilder ratingMessage = new StringBuilder();

            for (RatingRecord rating : ratings) {
                Map<String, String> ratingMap = new HashMap<>();
                map.put("feedback", rating.getFeedback());
                map.put("quality", rating.getQuality().toString());
                map.put("rating", rating.getRating().toString());

                ratingMessage.append(Template.apply(feedbackRating, ratingMap));
            }

            //Replace placeholders with answer and the ratings
            Map<String, String> answerMap = new HashMap<>();
            map.put("answer", answer.getAnswer());
            map.put("ratings", ratingMessage.toString());

            answerMessage.append(Template.apply(feedbackAnswer, answerMap));

            prevWorkerId = answer.getWorkerId();
        }

        //Send feedback to the last worker
        if (!answers.isEmpty()) {
            workerCount++;
            String workerMessage = Template.apply(feedbackMessage, map);
            sendFeedback(prevWorkerId, workerMessage);
        }

        LOGGER.trace("Completed sending feedback to " + workerCount + "workers");
    }

    private void sendFeedback(int workerID, String workerMessage) throws FeedbackException {
        if (workerOps.getWorker(workerID).isPresent()) {
            try {
                if (!workerOps.getWorker(workerID).get().getEmail().equals("")) {
                    handler.sendMail(workerOps.getWorker(workerID).get().getEmail(), "Feedback to your work", workerMessage);
                }
            } catch (MessagingException e) {
                LOGGER.error("Sending of feedback failed, because the mailsender cannot send the mails.");
                throw new FeedbackException("The mailHandler cannot send mails. It seems, that there is a problem with the mailserver or the properties file.");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Sending of feedback to worker " + workerID + " failed, because the mail address of this worker is invalid.");            }
        } else {
            LOGGER.error("Sending of feedback to worker " + workerID + " failed, because the worker cannot get found in the database.");
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
            LOGGER.error("Sending of feedback failed, because the message sources cannot be found.");
            throw new FeedbackException("The file at \"" + path + "\" couldn't be found. Please secure, that there is a file.");
        } catch (IOException e) {
            LOGGER.error("Sending of feedback failed, because the message sources are invalid.");
            throw new FeedbackException("The file at \"" + path + "\" couldn't be read. Please secure, that the file isn't corrupt");
        }
        return content.toString();
    }
}
