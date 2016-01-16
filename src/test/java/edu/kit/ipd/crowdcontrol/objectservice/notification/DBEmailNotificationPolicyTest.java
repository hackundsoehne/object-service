package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperation;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class DBEmailNotificationPolicyTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    DBEmailNotificationPolicy policy;
    Notification notification;
    @Mock
    private MailSender mailSender;

    @Mock
    private NotificationOperation nop;

    @Before
    public void setUp() throws Exception {
        policy = new DBEmailNotificationPolicy(mailSender, "mail@example.com", nop);
        notification = new Notification(5, "Test Notification",
                "This is a test notification", 60 * 60 * 24, 60 * 10, "SELECT", policy);
    }

    @Test
    public void testCheck() throws Exception {
        //TODO
        Result<Record> result = mock(Result.class);
        when(result.formatHTML()).thenReturn("parsed html");
        when(nop.runReadOnlySQL("SELECT")).thenReturn(result);
        policy.check(notification);
    }

    @Test
    public void testSend() throws Exception {

    }
}