package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import edu.ipd.kit.crowdcontrol.objectservice.database.DatabaseManager;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.Tables;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.records.NotificationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * contains all Operations needed to interact with the notifications-table.
 * @author LeanderK
 * @version 1.0
 */
public class NotificationOperation extends AbstractOperation {
    private final Connection readOnlyConnection;
    private final DSLContext readOnlyCreate;
    /**
     * creates an new instance of NotificationOperation.
     * @param manager the manager used to obtain all relevant information about the DB
     * @param username the username belonging to a read-only account on the DB
     * @param password the matching password belonging to a read-only accoint on the DB
     * @throws SQLException if there was a problem establishing a connection to the database
     */
    protected NotificationOperation(DatabaseManager manager, String username, String password) throws SQLException {
        super(manager.getContext());
        readOnlyConnection = DriverManager.getConnection(manager.getUrl(), username, password);
        readOnlyCreate = DSL.using(readOnlyConnection, manager.getContext().configuration().dialect());
    }

    /**
     * returns all the stored notifications
     * @return a list of notifications
     */
    public List<NotificationRecord> getAllNotifications() {
        return create.selectFrom(Tables.NOTIFICATION)
                .fetch();
    }

    /**
     * inserts a notification into the database.
     * @param record the record to insert
     * @return the resulting record (the primary key is guaranteed to be set)
     */
    public NotificationRecord insertNotification(NotificationRecord record) {
        return create.insertInto(Tables.NOTIFICATION)
                .set(record)
                .returning()
                .fetchOne();
    }

    /**
     * deletes a notification from the database
     * @param notificationID the primary key of the notification
     * @return true if deleted, false if not found
     */
    public boolean delteNotification(int notificationID) {
        return create.deleteFrom(Tables.NOTIFICATION)
                .where(Tables.NOTIFICATION.IDNOTIFICATION.eq(notificationID))
                .execute() == 1;
    }

    /**
     * updates the notifications lastSend field
     * @param notificationID the primary key of the notification
     * @return tre if updated, false if not found
     */
    public boolean updateLastSendForNotification(int notificationID) {
        return create.update(Tables.NOTIFICATION)
                .set(Tables.NOTIFICATION.LASTSENT, Timestamp.valueOf(LocalDateTime.now()))
                .where(Tables.NOTIFICATION.IDNOTIFICATION.eq(notificationID))
                .execute() == 1;
    }

    /**
     * runs a sql-query in read-only mode
     * @param sql the seq to execute in read-only mode
     * @return the Result of the query
     */
    public Result<Record> runReadOnlySQL(String sql) {
        return readOnlyCreate.fetch(sql);
    }
}
