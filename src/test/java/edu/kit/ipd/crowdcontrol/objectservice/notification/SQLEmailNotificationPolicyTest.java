package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationTokenRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class SQLEmailNotificationPolicyTest {
    private static final String TESTQUERY = "SELECT test query";
    private static final List<String> RECEIVER_EMAILS = new ArrayList<>(Arrays.asList("maila@example.com", "mailb@example.com"));
    SQLEmailNotificationPolicy policy;
    Notification notification;
    Result<Record> resultNoIdAndToken;
    Record record;
    DSLContext create;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    ArgumentCaptor<String> messageCaptor;
    @Mock
    private MailSender mailSender;
    @Mock
    private NotificationOperations notificationOperations;

    @Before
    public void setUp() throws Exception {
        policy = new SQLEmailNotificationPolicy(mailSender, notificationOperations);
        create = DSL.using(SQLDialect.MYSQL);
        // this could be any record from the db
        notification = new Notification(5, "Test Notification",
                "This is a test notification", 600, TESTQUERY, true, RECEIVER_EMAILS, policy);
        record = new NotificationTokenRecord(1, 2, "three", 5);
        resultNoIdAndToken = create.newResult();
        resultNoIdAndToken.add(record);
    }

    @Test
    public void testCheckNoIDAndTokenSuccess() throws Exception {
        when(notificationOperations.runReadOnlySQL(TESTQUERY)).thenReturn(resultNoIdAndToken);
        List<String> tokens = policy.check(notification);
        assertTrue(tokens != null && tokens.isEmpty());
    }

    @Test
    public void testCheckNegative() throws Exception {
        // return empty resultNoIdAndToken
        resultNoIdAndToken.clear();
        when(notificationOperations.runReadOnlySQL(TESTQUERY)).thenReturn(resultNoIdAndToken);
        List<String> tokens = policy.check(notification);

        assertEquals(null, tokens);
    }

    @Test
    public void testSend() throws Exception {
        policy.send(notification, Arrays.asList("TODO"));
        verify(mailSender).sendMail(eq(RECEIVER_EMAILS.get(0)), messageCaptor.capture(), messageCaptor.capture());
        verify(mailSender).sendMail(eq(RECEIVER_EMAILS.get(1)), messageCaptor.capture(), messageCaptor.capture());
        System.out.println(messageCaptor.getAllValues().toString());
    }
}