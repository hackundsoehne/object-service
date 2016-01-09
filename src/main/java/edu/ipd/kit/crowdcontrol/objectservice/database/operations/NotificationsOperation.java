package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

/**
 * contains
 * @author LeanderK
 * @version 1.0
 */
public class NotificationsOperation extends AbstractOperation {
    protected NotificationsOperation(DSLContext create, String username, String password) {
        super(create);
    }

    public List<NotificationRecord> getAllNotifications() {
        return null;
    }

    public boolean insertNotification(NotificationRecord record) {
        return false;
    }

    public boolean updateLastSendForNotification(int notificationID) {
        return false;
    }

    public Result<Record> rundReadOnlySQL(String sql) {
        return null;
    }
}
