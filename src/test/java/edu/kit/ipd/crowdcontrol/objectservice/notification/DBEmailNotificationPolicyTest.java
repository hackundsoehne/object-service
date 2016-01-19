package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperation;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
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
    NotificationRecord record;
    Result<Record> result;

    @Mock
    private MailSender mailSender;

    @Mock
    private NotificationOperation notificationOperation;

    @Before
    public void setUp() throws Exception {
        policy = new DBEmailNotificationPolicy(mailSender, "mail@example.com", notificationOperation);
        notification = new Notification(5, "Test Notification",
                "This is a test notification", 60 * 60 * 24, 60 * 10, "SELECT", policy);

        DSLContext create = DSL.using(SQLDialect.MYSQL);
        // this could be any record from the db
        record = new NotificationRecord(5, "Test Notification",
                "This is a test notification", 60 * 60 * 24, 60 * 10, "SELECT", Timestamp.from(Instant.now()));
        result = create.newResult(Tables.NOTIFICATION);
        result.add(record);
    }

    @Test
    public void testCheck() throws Exception {
        // TODO fix
        when(notificationOperation.runReadOnlySQL("SELECT")).thenReturn(result);
        Result<Record> token = policy.check(notification);

        assertEquals(result, token);
    }

    @Test
    public void testSend() throws Exception {
        policy.send(notification, result);
    }
}