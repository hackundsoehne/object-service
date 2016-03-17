package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Worker;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import org.jooq.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Tests the feedback sender
 *
 * @author Felix Rittler
 */
public class FeedbackCreatorTest {

    private AnswerRatingOperations answerRatingOperations;
    private ExperimentOperations experimentOperations;
    private WorkerOperations workerOperations;
    private FeedbackCreator sender;

    @Before
    public void setUp() throws Exception {
        answerRatingOperations = mock(AnswerRatingOperations.class);
        experimentOperations = mock(ExperimentOperations.class);
        workerOperations = mock(WorkerOperations.class);

        sender = new FeedbackCreator(answerRatingOperations, experimentOperations, workerOperations);
    }

    @Test
    public void testSendFeedback() throws Exception {
        List<AnswerRecord> answers = mock(Result.class);
        doReturn(answers).when(answerRatingOperations).getAnswersOfWorkerFromExperiment(0,0);
        Iterator<AnswerRecord> answerIterator = mock(Iterator.class);

        AnswerRecord a1 = new AnswerRecord();
        a1.setWorkerId(0);
        a1.setAnswer("baz1");
        a1.setSystemResponse("awes0me Message");

        AnswerRecord a2 = new AnswerRecord();
        a2.setWorkerId(0);
        a2.setAnswer("baz2");
        a2.setSystemResponse(null);

        WorkerRecord w1 = new WorkerRecord(0, null, null, "coolcrowd@42.pi", 0, "");
        Optional<WorkerRecord> o1 = Optional.of(w1);
        when(workerOperations.getWorker(0)).thenReturn(o1);

        RatingRecord ratingAnswer1x1 = new RatingRecord();
        ratingAnswer1x1.setFeedback("Foobar1");
        ratingAnswer1x1.setQuality(1);
        ratingAnswer1x1.setRating(1);

        RatingRecord ratingAnswer1x2 = new RatingRecord();
        ratingAnswer1x2.setFeedback("Foobar2");
        ratingAnswer1x2.setQuality(2);
        ratingAnswer1x2.setRating(2);

        RatingRecord ratingAnswer2x1 = new RatingRecord();
        ratingAnswer2x1.setFeedback("Foobar3");
        ratingAnswer2x1.setQuality(3);
        ratingAnswer2x1.setRating(3);

        RatingRecord ratingAnswer2x2 = new RatingRecord();
        ratingAnswer2x2.setFeedback("Foobar4");
        ratingAnswer2x2.setQuality(4);
        ratingAnswer2x2.setRating(4);

        Result<RatingRecord> ratingListAnswer1 = mock(Result.class);
        Iterator<RatingRecord> itRatingsAnswer1 = mock(Iterator.class);
        when(ratingListAnswer1.iterator()).thenReturn(itRatingsAnswer1);
        when(itRatingsAnswer1.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(itRatingsAnswer1.next()).thenReturn(ratingAnswer1x1).thenReturn(ratingAnswer1x2);

        Result<RatingRecord> ratingListAnswer2 = mock(Result.class);
        Iterator<RatingRecord> itRatingsAnswer2 = mock(Iterator.class);
        when(ratingListAnswer2.iterator()).thenReturn(itRatingsAnswer2);
        when(itRatingsAnswer2.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(itRatingsAnswer2.next()).thenReturn(ratingAnswer2x1).thenReturn(ratingAnswer2x2);

        when(answerRatingOperations.getRatingsOfAnswer(a1)).thenReturn(ratingListAnswer1);
        when(answerRatingOperations.getRatingsOfAnswer(a2)).thenReturn(ratingListAnswer2);

        when(answers.iterator()).thenReturn(answerIterator);
        when(answerIterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(answerIterator.next()).thenReturn(a1).thenReturn(a2);

        ExperimentRecord exp = new ExperimentRecord();
        exp.setTitle("foobarExperiment");
        Optional<ExperimentRecord> expOpt = Optional.of(exp);
        doReturn(expOpt).when(experimentOperations).getExperiment(0);

        String message2 = Utils.loadFile("/feedback/workermessage1.txt");
        String feedback = sender.getFeedback(0,0);
        Assert.assertEquals(feedback, message2);
    }

    @Test
    public void testNoAnswers() throws Exception {
        List<AnswerRecord> answers = mock(Result.class);
        doReturn(answers).when(answerRatingOperations).getAnswersOfWorkerFromExperiment(0,0);
        Iterator<AnswerRecord> it = mock(Iterator.class);
        when(answers.iterator()).thenReturn(it);
        when(it.hasNext()).thenReturn(false);
        when(answers.isEmpty()).thenReturn(true);

        ExperimentRecord exp = mock(ExperimentRecord.class);
        when(exp.getTitle()).thenReturn("foobarExperiment");
        Optional<ExperimentRecord> expOpt = Optional.of(exp);
        doReturn(expOpt).when(experimentOperations).getExperiment(0);
        Assert.assertEquals("", sender.getFeedback(0,0));
    }


}
