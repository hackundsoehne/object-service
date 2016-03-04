package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Tests the feedback sender
 * @author Felix Rittler
 */
public class FeedbackSenderTest {

    private MailHandler handler;
    private AnswerRatingOperations answerRatingOperations;
    private ExperimentOperations experimentOperations;
    private WorkerOperations workerOperations;
    private FeedbackSender sender;

    @Before
    public void setUp() throws Exception {
        handler = mock(MailHandler.class);
        answerRatingOperations = mock(AnswerRatingOperations.class);
        experimentOperations = mock(ExperimentOperations.class);
        workerOperations = mock(WorkerOperations.class);

        sender = new FeedbackSender(handler, answerRatingOperations, experimentOperations, workerOperations);
    }

    @Test
    public void testSendFeedback() throws Exception {
        Result<AnswerRecord> answers = mock(Result.class);
        doReturn(answers).when(answerRatingOperations).getAnswersOfExperiment(0);
        Iterator<AnswerRecord> it = mock(Iterator.class);

        AnswerRecord a1 = mock(AnswerRecord.class);
        when(a1.getWorkerId()).thenReturn(0);
        when(a1.getAnswer()).thenReturn("baz1");

        AnswerRecord a2 = mock(AnswerRecord.class);
        when(a2.getWorkerId()).thenReturn(1);
        when(a2.getAnswer()).thenReturn("baz2");

        AnswerRecord a3 = mock(AnswerRecord.class);
        when(a3.getWorkerId()).thenReturn(0);
        when(a3.getAnswer()).thenReturn("baz3");

        AnswerRecord a4 = mock(AnswerRecord.class);
        when(a4.getWorkerId()).thenReturn(0);
        when(a4.getAnswer()).thenReturn("baz4");

        WorkerRecord w1 = mock(WorkerRecord.class);
        Optional<WorkerRecord> o1 = Optional.of(w1);
        when(workerOperations.getWorker(0)).thenReturn(o1);
        when(w1.getEmail()).thenReturn("");

        WorkerRecord w2 = mock(WorkerRecord.class);
        Optional<WorkerRecord> o2 = Optional.of(w2);
        when(workerOperations.getWorker(1)).thenReturn(o2);
        when(w2.getEmail()).thenReturn("foobar@baz.xyz");

        RatingRecord r1 = new RatingRecord();
        r1.setFeedback("Foobar1");
        r1.setQuality(1);
        r1.setRating(1);

        RatingRecord r2 = new RatingRecord();
        r2.setFeedback("Foobar2");
        r2.setQuality(2);
        r2.setRating(2);

        RatingRecord r3 = new RatingRecord();
        r3.setFeedback("Foobar3");
        r3.setQuality(3);
        r3.setRating(3);

        RatingRecord r4 = new RatingRecord();
        r4.setFeedback("Foobar4");
        r4.setQuality(4);
        r4.setRating(4);

        RatingRecord r5 = new RatingRecord();
        r5.setFeedback("Foobar5");
        r5.setQuality(5);
        r5.setRating(5);

        RatingRecord r6 = new RatingRecord();
        r6.setFeedback("Foobar6");
        r6.setQuality(6);
        r6.setRating(6);

        RatingRecord r7 = new RatingRecord();
        r7.setFeedback("Foobar7");
        r7.setQuality(7);
        r7.setRating(7);

        RatingRecord r8 = new RatingRecord();
        r8.setFeedback("Foobar8");
        r8.setQuality(8);
        r8.setRating(8);

        Result<RatingRecord> l1 = mock(Result.class);
        Iterator<RatingRecord> it1 = mock(Iterator.class);
        when(l1.iterator()).thenReturn(it1);
        when(it1.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it1.next()).thenReturn(r1).thenReturn(r2);

        Result<RatingRecord> l2 = mock(Result.class);
        Iterator<RatingRecord> it2 = mock(Iterator.class);
        when(l2.iterator()).thenReturn(it2);
        when(it2.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it2.next()).thenReturn(r3).thenReturn(r4);

        Result<RatingRecord> l3 = mock(Result.class);
        Iterator<RatingRecord> it3 = mock(Iterator.class);
        when(l3.iterator()).thenReturn(it3);
        when(it3.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when((it3.next())).thenReturn(r5).thenReturn(r6);

        Result<RatingRecord> l4 = mock(Result.class);
        Iterator<RatingRecord> it4 = mock(Iterator.class);
        when(l4.iterator()).thenReturn(it4);
        when(it4.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it4.next()).thenReturn(r7).thenReturn(r8);

        when(answerRatingOperations.getRatingsOfAnswer(a1)).thenReturn(l1);
        when(answerRatingOperations.getRatingsOfAnswer(a2)).thenReturn(l2);
        when(answerRatingOperations.getRatingsOfAnswer(a3)).thenReturn(l3);
        when(answerRatingOperations.getRatingsOfAnswer(a4)).thenReturn(l4);

        when(answers.iterator()).thenReturn(it);
        when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it.next()).thenReturn(a1).thenReturn(a2).thenReturn(a3).thenReturn(a4);

        ExperimentRecord exp = mock(ExperimentRecord.class);
        Optional<ExperimentRecord> expOpt = Optional.of(exp);
        doReturn(expOpt).when(experimentOperations).getExperiment(0);

        sender.sendFeedback(0);


    }
}
