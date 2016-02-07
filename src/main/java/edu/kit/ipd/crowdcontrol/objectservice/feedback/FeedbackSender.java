package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.Range;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.jooq.Result;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sends feedback to workers according to their answers.
 *
 * @author Felix Rittler
 */
public class FeedbackSender {

    private MailHandler handler;
    private AnswerRatingOperations answerOps;
    private ExperimentOperations expOps;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> schedule = null;

    public FeedbackSender(MailHandler handler, AnswerRatingOperations answerOps, ExperimentOperations expOps) {
        this.handler = handler;
        this.answerOps = answerOps;
        this.expOps = expOps;
    }

    public void sendFeedback(int expId) throws ExperimentNotFoundException {
        Optional<ExperimentRecord> exp = expOps.getExperiment(expId);
        int answerCount = 0;
        if (exp.isPresent()) {
            answerCount = exp.get().getNeededAnswers();
        } else {
            throw new ExperimentNotFoundException();
        }
        Range<AnswerRecord, Integer> answers = answerOps.getAnswersFrom(expId, 0, true, answerCount);
    }

    private String buildFeedbackMessage(WorkerRecord worker) {
        return null;
    }
}
