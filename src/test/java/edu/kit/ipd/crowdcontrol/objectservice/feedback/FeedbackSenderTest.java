package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.jooq.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import javax.mail.MessagingException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Tests the feedback sender
 *
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
        List<AnswerRecord> answers = mock(Result.class);
        doReturn(answers).when(answerRatingOperations).getAnswersOfExperimentOfWorker(0,0);
        Iterator<AnswerRecord> it = mock(Iterator.class);

        AnswerRecord a1 = new AnswerRecord();
        a1.setWorkerId(0);
        a1.setAnswer("baz1");

        AnswerRecord a2 = new AnswerRecord();
        a2.setWorkerId(0);
        a2.setAnswer("baz2");

        WorkerRecord w1 = mock(WorkerRecord.class);
        Optional<WorkerRecord> o1 = Optional.of(w1);
        when(workerOperations.getWorker(0)).thenReturn(o1);
        when(w1.getEmail()).thenReturn("coolcrowd@42.pi");

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

        when(answerRatingOperations.getRatingsOfAnswer(a1)).thenReturn(l1);
        when(answerRatingOperations.getRatingsOfAnswer(a2)).thenReturn(l2);

        when(answers.iterator()).thenReturn(it);
        when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it.next()).thenReturn(a1).thenReturn(a2);

        ExperimentRecord exp = mock(ExperimentRecord.class);
        when(exp.getTitle()).thenReturn("foobarExperiment");
        Optional<ExperimentRecord> expOpt = Optional.of(exp);
        doReturn(expOpt).when(experimentOperations).getExperiment(0);


        doNothing().when(handler).sendMail(anyString(), anyString(), anyString());




        String message2 = sender.loadMessage("src/test/resources/feedback/workermessage1.txt");
        Assert.assertEquals(sender.getFeedback(0,0), message2);
    }

    @Test
    public void testNoAnswers() throws Exception {
        List<AnswerRecord> answers = mock(Result.class);
        doReturn(answers).when(answerRatingOperations).getAnswersOfExperimentOfWorker(0,0);
        Iterator<AnswerRecord> it = mock(Iterator.class);
        when(answers.iterator()).thenReturn(it);
        when(it.hasNext()).thenReturn(false);
        when(answers.isEmpty()).thenReturn(true);
        doThrow(new MessagingException()).when(handler).sendMail(anyString(), anyString(), anyString());

        ExperimentRecord exp = mock(ExperimentRecord.class);
        when(exp.getTitle()).thenReturn("foobarExperiment");
        Optional<ExperimentRecord> expOpt = Optional.of(exp);
        doReturn(expOpt).when(experimentOperations).getExperiment(0);
        Assert.assertEquals("", sender.getFeedback(0,0));
    }


}
