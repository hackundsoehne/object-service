package edu.kit.ipd.crowdcontrol.objectservice.notification;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.NotificationTokenRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSender;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The SQLEmailNotificationPolicy checks given queries in a SQL-Database and sends notifications via email.
 *
 * @author Simon Korz
 * @version 1.0
 */
public class SQLEmailNotificationPolicy extends NotificationPolicy<List<String>> {
    private final MailSender mailSender;
    private final NotificationOperations operations;


    /**
     * @param mailSender an implementation of the MailSender interface
     * @param operations  instance of the notification operations
     */
    public SQLEmailNotificationPolicy(MailSender mailSender, NotificationOperations operations) {
        super(operations);
        this.mailSender = mailSender;
        this.operations = operations;
    }

    /**
     * Checks if the record has the desired form of:
     * <p>
     * |id |token| <br>
     * |int|string|
     * </p>
     *
     * @param record the record to check
     * @return true if the record has the desired form, else false
     */
    private boolean recordHasIdAndTokenField(Record record) {
        Field<Integer> idField = DSL.field("id", Integer.class);
        Field<String> tokenField = DSL.field("token", String.class);

        return (record.field(idField) != null) && (record.field(tokenField) != null);
    }

    @Override
    protected List<String> check(Notification notification) {
        // peek on first record
        Record firstRecord;
        try (Stream<Record> recordStream = operations.runReadOnlySQL(notification.getQuery())) {
            firstRecord = recordStream.findFirst().orElse(null);
        }
        if (firstRecord != null) {
            if (recordHasIdAndTokenField(firstRecord)) {
                // go on and fetch all. might be inefficient if there is only one record to fetch.
                // but in the common case there are more than one. Most efficient would be to check only the first time
                // a query is specified but that's too much work for now.

                // the records have the required columns, so we can bring them in a handier format
                Map<Integer, NotificationTokenRecord> tokenRecords;
                try (Stream<Record> recordStream = operations.runReadOnlySQL(notification.getQuery())) {
                    tokenRecords = recordStream.map(record -> new NotificationTokenRecord(
                            null,
                            record.getValue("id", Integer.class),
                            record.getValue("token", String.class),
                            notification.getId()))
                            .collect(Collectors.toMap(NotificationTokenRecord::getResultId, Function.identity()));
                }
                List<String> newTokenList = operations.diffTokenRecords(tokenRecords, notification.getId()).stream()
                        .map(NotificationTokenRecord::getResultToken)
                        .collect(Collectors.toList());
                if (!newTokenList.isEmpty()) {
                    return newTokenList;
                }
            } else {
                // the result has any other form and we cannot provide any tokens
                return Collections.emptyList();
            }
        }
        return null;
    }

    @Override
    protected void send(Notification notification, List<String> tokens) {
        StringBuilder message = new StringBuilder();

        // TODO fancy pancy html + css styled body with beautiful design and chocolate flavor ...if there is time
        if (tokens.isEmpty()) {
            message.append(notification.getDescription());
        } else {
            // search for placeholders in description
            Map<String, String> placeholderMap = new HashMap<>();
            placeholderMap.put("tokens", concatTokens(tokens));
            try {
                message.append(Template.apply(notification.getDescription(), placeholderMap));
            } catch (IllegalArgumentException iae) {
                message.append(notification.getDescription());
            }
        }

        String subject = "[CrowdControl] " + notification.getName();
        for (String receiver : notification.getReceiverEmails()) {
            try {
                mailSender.sendMail(receiver, subject, message.toString());
            } catch (Exception e) {
                throw new NotificationNotSentException(String.format("notification with id=%s could not be sent to %s",
                        notification.getId(), receiver), e);
            }
        }
    }

    private String concatTokens(List<String> tokens) {
        StringBuilder concatedTokens = new StringBuilder();
        if (tokens.size() > 1) {
            int i;
            for (i = 0; i < tokens.size() - 1; i++) {
                concatedTokens.append(tokens.get(i));
                concatedTokens.append(", ");
            }
            concatedTokens.append("und ");
            concatedTokens.append(tokens.get(i));
        } else {
            concatedTokens.append(tokens.get(0));
        }
        return concatedTokens.toString();
    }
}

