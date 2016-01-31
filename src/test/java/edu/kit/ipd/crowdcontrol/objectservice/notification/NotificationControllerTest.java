package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Notification;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Simon Korz
 * @version 1.0
 */
public class NotificationControllerTest {
    private static final int CHECKPERIOD = 1;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private NotificationController notificationController;
    private edu.kit.ipd.crowdcontrol.objectservice.proto.Notification notificationProto;
    private edu.kit.ipd.crowdcontrol.objectservice.notification.Notification notification;
    @Mock
    private NotificationPolicy<Result<Record>> policy;

    @Mock
    private NotificationOperations notificationOperations;

    @Before
    public void setUp() throws Exception {
        notificationController = new NotificationController(notificationOperations, policy);
        Notification.Builder builder = Notification.newBuilder();
        builder.setId(5);
        builder.setName("Test Notification");
        builder.setDescription("This is a test notification");
        builder.setSendThreshold(60 * 60 * 24);
        builder.setCheckPeriod(CHECKPERIOD);
        builder.setQuery("SELECT");
        notificationProto = builder.buildPartial();

        notification = new edu.kit.ipd.crowdcontrol.objectservice.notification.Notification(notificationProto.getId(),
                notificationProto.getName(), notificationProto.getDescription(), notificationProto.getSendThreshold(),
                notificationProto.getCheckPeriod(), notificationProto.getQuery(), policy);
    }

    @Test
    public void testCreateNotification() throws Exception {
        notificationController.createNotification(notificationProto);
        // wait for a second invocation
        Thread.sleep(CHECKPERIOD * 1000 + 10);
        verify(policy, times(2)).invoke(eq(notification));
    }

    @Test
    public void testDeleteNotification() throws Exception {
        //depends on created notification
        testCreateNotification();
        notificationController.deleteNotification(notificationProto);
        // wait for another invocation that hopefully never occurs
        Thread.sleep(CHECKPERIOD * 1000 + 10);
        verifyNoMoreInteractions(policy);
    }


}