package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationTokenRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The SQLEmailNotificationPolicy checks given queries in a SQL-Database and sends notifications via email.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class SQLEmailNotificationPolicy extends NotificationPolicy<List<String>> {
    private MailSender mailSender;
    private NotificationOperations operations;


    /**
     * @param mailSender an implementation of the MailSender interface
     * @param operations  instance of the notification operations
     */
    public SQLEmailNotificationPolicy(MailSender mailSender, NotificationOperations operations) {
        super(operations);
        this.mailSender = mailSender;
        this.operations = operations;
    }

    private boolean resultHasIdAndTokenField(Result<Record> result) {
        Field<?> idField = result.field("id");
        Field<?> tokenField = result.field("token");
        if (idField != null && tokenField != null) {
            DataType<?> idType = idField.getDataType();
            DataType<?> tokenType = tokenField.getDataType();
            if (idType.isNumeric() && tokenType.isString()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<String> check(Notification notification) {
        Result<Record> result = operations.runReadOnlySQL(notification.getQuery());
        List<String> tokenList = new ArrayList<>();
        if (result.isNotEmpty()) {
            if (resultHasIdAndTokenField(result)) {
                // TODO diffi diffi
                Map<Integer, NotificationTokenRecord> tokenRecords = result.stream()
                        .map(record -> new NotificationTokenRecord(null, record.getValue("id", Integer.class),
                                record.getValue("token", String.class), notification.getID())
                        )
                        .collect(Collectors.toMap(NotificationTokenRecord::getResultId, Function.identity()));
                return operations.diffTokenRecords(tokenRecords, notification.getID()).stream()
                        .map(NotificationTokenRecord::getResultToken)
                        .collect(Collectors.toList());
            }
            return tokenList;
        } else {
            return null;
        }
    }

    @Override
    protected void send(Notification notification, List<String> tokens) {
        StringBuilder message = new StringBuilder();
        message.append(notification.getDescription());
        message.append("\n\n");


        String subject = "[CrowdControl Notification] " + notification.getName();
        try {
            for (String receiver : notification.getReceiverEmails())
            mailSender.sendMail(receiver, subject, message.toString());
        } catch (Exception e) {
            throw new NotificationNotSentException(e);
        }
    }
}

