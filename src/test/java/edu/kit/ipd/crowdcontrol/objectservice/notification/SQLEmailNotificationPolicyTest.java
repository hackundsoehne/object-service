package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationTokenRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class SQLEmailNotificationPolicyTest {
    private static final String TESTQUERY = "SELECT test query";
    private static final List<String> RECEIVER_EMAILS = Arrays.asList("maila@example.com", "mailb@example.com");
    static SQLEmailNotificationPolicy policy;
    static Notification notification;
    static Result<Record> resultNoIdAndToken;
    static Result<Record> resultWithIdAndToken;
    static Map<Integer, NotificationTokenRecord> tokenRecordMap = new HashMap<>();
    static Record record;
    static DSLContext create;

    static NotificationOperations notificationOperations;
    static MailSender mailSender;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    ArgumentCaptor<String> messageCaptor;

    @BeforeClass
    public static void setUp() throws Exception {
        mailSender = Mockito.mock(MailSender.class);
        notificationOperations = Mockito.mock(NotificationOperations.class);
        policy = new SQLEmailNotificationPolicy(mailSender, notificationOperations);
        create = DSL.using(SQLDialect.MYSQL);
        // this could be any record from the db
        notification = new Notification(5, "Test Notification",
                "Die Tests {{tokens}} wurden erfolgreich abgeschlossen!", 600, TESTQUERY, true, RECEIVER_EMAILS, policy);
    }

    private static Stream<Record> getResulWithIdAndToken() {
        Field<Integer> idField = DSL.field("id", Integer.class);
        Field<String> tokenField = DSL.field("token", String.class);
        resultWithIdAndToken = create.newResult();

        for (int i = 0; i < 10; i++) {
            String token = "my_token_" + Integer.toString(i);
            Record2<Integer, String> idTokenRecord =
                    create.newRecord(idField, tokenField);
            idTokenRecord.setValue(idField, i);
            idTokenRecord.setValue(tokenField, token);
            resultWithIdAndToken.add(idTokenRecord);
            tokenRecordMap.put(i, new NotificationTokenRecord(null, i, token, notification.getId()));
        }
        return resultWithIdAndToken.stream();
    }

    private static Stream<Record> getResultNoIdAndToken() {
        record = new NotificationTokenRecord(1, 2, "three", 5);
        resultNoIdAndToken = create.newResult();
        resultNoIdAndToken.add(record);
        return resultNoIdAndToken.stream();
    }
    @Before
    public void setUpClear() throws Exception {
        Mockito.reset(mailSender);
        Mockito.reset(notificationOperations);
    }

    @Test
    public void testCheckNoIDAndTokenSuccess() throws Exception {
        when(notificationOperations.runReadOnlySQL(TESTQUERY)).thenReturn(getResultNoIdAndToken());
        List<String> tokens = policy.check(notification);
        assertTrue(tokens != null && tokens.isEmpty());
    }

    @Test
    public void testCheckEmptyResult() throws Exception {
        Result<Record> emptyResult = create.newResult();
        when(notificationOperations.runReadOnlySQL(TESTQUERY)).thenReturn(emptyResult.stream());
        List<String> tokens = policy.check(notification);
        assertNull(tokens);
    }

    @Test
    public void testCheckWithIdAndToken2NewRecords() throws Exception {
        when(notificationOperations.runReadOnlySQL(TESTQUERY)).thenReturn(getResulWithIdAndToken(), getResulWithIdAndToken());
        when(notificationOperations.diffTokenRecords(tokenRecordMap, notification.getId()))
                //only return the first 2 records
                .thenReturn(tokenRecordMap.values().stream().limit(2).collect(Collectors.toList()));
        List<String> tokens = policy.check(notification);
        assertEquals(Arrays.asList("my_token_0", "my_token_1"), tokens);
    }

    @Test
    public void testCheckWithIdAndTokenNoNewRecords() throws Exception {
        when(notificationOperations.runReadOnlySQL(TESTQUERY)).thenReturn(getResulWithIdAndToken(), getResulWithIdAndToken());
        when(notificationOperations.diffTokenRecords(tokenRecordMap, notification.getId()))
                .thenReturn(Collections.emptyList());
        List<String> tokens = policy.check(notification);
        assertNull(tokens);
    }


    @Test
    public void testSendWithTokens() throws Exception {
        policy.send(notification, Arrays.asList("eins", "zwei", "drei"));
        verify(mailSender).sendMail(eq(RECEIVER_EMAILS.get(0)), messageCaptor.capture(), messageCaptor.capture());
        verify(mailSender).sendMail(eq(RECEIVER_EMAILS.get(1)), messageCaptor.capture(), messageCaptor.capture());
        System.out.println(messageCaptor.getAllValues().toString());
    }

    @Test
    public void testSendNoTokens() throws Exception {
        policy.send(notification, Collections.emptyList());
        verify(mailSender).sendMail(eq(RECEIVER_EMAILS.get(0)), messageCaptor.capture(), messageCaptor.capture());
        verify(mailSender).sendMail(eq(RECEIVER_EMAILS.get(1)), messageCaptor.capture(), messageCaptor.capture());
        System.out.println(messageCaptor.getAllValues().toString());
    }
}