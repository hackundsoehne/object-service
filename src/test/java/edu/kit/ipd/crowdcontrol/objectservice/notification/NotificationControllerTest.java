package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

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
    private NotificationPolicy<List<String>> policy;

    @Mock
    private NotificationOperations notificationOperations;

    @Before
    public void setUp() throws Exception {
        notificationController = new NotificationController(notificationOperations, policy);

        ArrayList<String> receiverEmails = new ArrayList<>();
        receiverEmails.add("mail-a@example.com");
        receiverEmails.add("mail-b@example.com");

        notification = new edu.kit.ipd.crowdcontrol.objectservice.notification.Notification(3, "Test Notification",
                "This describes the Test notification", CHECKPERIOD, "SELECT test", false, receiverEmails, policy);

        notificationProto = notification.toProtobuf();
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