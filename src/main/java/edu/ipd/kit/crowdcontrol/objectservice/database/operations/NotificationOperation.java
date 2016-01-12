package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

/**
 * contains all Operations needed to interact with the notifications-table.
 * @author LeanderK
 * @version 1.0
 */
public class NotificationOperation extends AbstractOperation {
    /**
     * creates an new instance of NotificationOperation.
     * @param create the context used for normal communication with the DB
     * @param username the username belonging to a read-only account on the DB
     * @param password the matching passwort belonging to a read-only accoint on the DB
     */
    protected NotificationOperation(DSLContext create, String username, String password) {
        super(create);
    }

    /**
     * returns all the stored notifications
     * @return a list of notifications
     */
    public List<NotificationRecord> getAllNotifications() {
        return null;
    }

    /**
     * inserts a notification into the database.
     * @param record the record to insert
     * @return the primary-key or -1
     */
    public int insertNotification(NotificationRecord record) {
        return -1;
    }

    /**
     * deletes a notification from the database
     * @param notificationID the primary key of the notification
     * @return true if deleted, false if not found
     */
    public boolean delteNotification(int notificationID) {
        return false;
    }

    /**
     * updates the notifications lastSend field
     * @param notificationID the primary key of the notification
     * @return tre if updated, false if not found
     */
    public boolean updateLastSendForNotification(int notificationID) {
        return false;
    }

    /**
     * runs a sql-query in read-only mode
     * @param sql the seq to execute in read-only mode
     * @return the Result of the query
     */
    public Result<Record> runReadOnlySQL(String sql) {
        return null;
    }
}
