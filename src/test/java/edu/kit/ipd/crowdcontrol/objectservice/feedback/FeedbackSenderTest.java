package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Tests the feedback sender
 * @author Felix Rittler
 */
public class FeedbackSenderTest {

    @Before
    public void setUp() throws Exception {
        MailHandler handler = mock(MailHandler.class);
        AnswerRatingOperations answerRatingOperations = mock(AnswerRatingOperations.class);
        ExperimentOperations experimentOperations = mock(ExperimentOperations.class);
        WorkerOperations workerOperations = mock(WorkerOperations.class);

        FeedbackSender sender = new FeedbackSender(handler, answerRatingOperations, experimentOperations, workerOperations);
    }

    @Test
    public void testSendFeedback() throws Exception {

    }
}
