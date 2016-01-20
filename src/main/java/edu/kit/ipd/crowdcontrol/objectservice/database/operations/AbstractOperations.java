package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.google.protobuf.MessageOrBuilder;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * superclass of all database operations. Contains abstractions and helper-methods.
 * @author LeanderK
 * @version 1.0
 */
public abstract class AbstractOperations {
    protected final DSLContext create;

    /**
     * creates a new AbstractOperation
     * @param create the context to use to communicate with the database
     */
    protected AbstractOperations(DSLContext create) {
        this.create = create;
    }

    /**
     * executes the function if the experiment is not running.
     * @param experimentID the id of the experiment
     * @param function the function to execute
     * @param <R> the return type
     * @return the result of the function
     */
    protected <R> R doIfNotRunning(int experimentID, Function<Configuration, R> function) {
        return create.transactionResult(trans -> {
            boolean running = DSL.using(trans).fetchExists(
                    DSL.selectFrom(Tables.TASK)
                            .where(Tables.TASK.EXPERIMENT.eq(experimentID))
                            .and(Tables.TASK.STATUS.eq(TaskStatus.running).or(Tables.TASK.STATUS.eq(TaskStatus.stopping)))
            );
            if (!running) {
                return function.apply(trans);
            } else {
                //TODO other exception?
                throw new IllegalArgumentException("Experiment is running");
            }
        });
    }

    /**
     * returns whether the MessageOrBuilder has the passed field.
     * @param messageOrBuilder the MessageOrBuilder to check on
     * @param field the field to exist
     * @return tre if it has the field, false if not
     */
    protected boolean hasField(MessageOrBuilder messageOrBuilder, int field) {
        return messageOrBuilder.hasField(messageOrBuilder.getDescriptorForType().findFieldByNumber(field));
    }

    /**
     * this method returns a range of results from a passed query.
     * @param <R> the type of the records
     * @param query the query to use
     * @param primaryKey the primary key used to index the records inside the range
     * @param start the exclusive start, when the associated record does not fulfill the conditions of the passed query
     *              or is not existing, the range-object communicates that there are no elements left (next = true) or
     *              right (next=false) of the range.
     * @param next whether the Range is right (true) or left of the primary key (false) assuming natural order
     * @param limit the max. amount of the range, may be smaller
     * @return an instance of Range
     * @see #getNextRange(SelectWhereStep, Field, Object, boolean, int, Comparator)
     */
    protected <R extends org.jooq.Record> Range<R, Integer> getNextRange(SelectWhereStep<R> query, Field<Integer> primaryKey,
                                                                      Integer start, boolean next, int limit) {
        return getNextRange(query, primaryKey, start, next, limit, Comparator.naturalOrder());
    }

    /**
     * this method returns a range of results from a passed query.
     * @param query the query to use
     * @param primaryKey the primary key used to index the records inside the range
     * @param start the exclusive start, when the associated record does not fulfill the conditions of the passed query
     *              or is not existing, the range-object communicates that there are no elements left (next = true) or
     *              right (next=false) of the range.
     * @param next whether the Range is right (true) or left of the primary key (false) assuming natural order
     * @param limit the max. amount of the range, may be smaller
     * @param <R> the type of the records
     * @return an instance of Range
     * @see #getNextRange(SelectWhereStep, Field, Object, boolean, int, Comparator)
     */
    protected <R extends org.jooq.Record> Range<R, Integer> getNextRange(SelectConditionStep<R> query, Field<Integer> primaryKey,
                                                                         Integer start, boolean next, int limit) {
        return getNextRange(query, primaryKey, start, next, limit, Comparator.naturalOrder());
    }

    /**
     * this method returns a range of results from a passed query.
     * @param query the query to use
     * @param primaryKey the primary key used to index the records inside the range
     * @param start the exclusive start, when the associated record does not fulfill the conditions of the passed query
     *              or is not existing, the range-object communicates that there are no elements left (next = true) or
     *              right (next=false) of the range.
     * @param next whether the Range is right (true) or left of the primary key (false) assuming natural order
     * @param limit the max. amount of the range, may be smaller
     * @param sort the comparator used to sort the elements
     * @param <R> the type of the records
     * @param <K> the type of the primary-key
     * @return an instance of Range
     * @see #getNextRange(SelectWhereStep, Field, Object, boolean, int, Comparator)
     */
    protected <R extends org.jooq.Record, K> Range<R, K> getNextRange(SelectWhereStep<R> query, Field<K> primaryKey,
                                                            K start, boolean next, int limit, Comparator<K> sort) {
        return getNextRange(query.where(true), primaryKey, start, next, limit, sort);
    }

    /**
     * this method returns a range of results from a passed query.
     * The range is indexed by a primary key to support good performance for bigger datasets. The resulting range always
     * starts after the passed start and tries to get the amount of records specified with limit. The next-modifier decides
     * whether the range should be after or before the start-parameter.
     * An application for this method could be paging where for a specific query you only want a specific range of results.
     * @param query the query to use
     * @param primaryKey the primary key used to index the records inside the range
     * @param start the exclusive start, when the associated record does not fulfill the conditions of the passed query
     *              or is not existing, the range-object communicates that there are no elements left (next = true) or
     *              right (next=false) of the range.
     * @param next whether the Range is right (true) or left of the primary key (false) assuming natural order
     * @param limit the max. amount of the range, may be smaller
     * @param sort the comparator used to sort the elements
     * @param <R> the type of the records
     * @param <K> the type of the primary-key
     * @return an instance of Range
     */
    protected <R extends org.jooq.Record, K> Range<R, K> getNextRange(SelectConditionStep<R> query, Field<K> primaryKey,
                                                            K start, boolean next, int limit, Comparator<K> sort) {
        Condition primaryKeyCondition = next
                            ? primaryKey.greaterOrEqual(start)
                            : primaryKey.lessOrEqual(start);

        SortField<K> sortField = next
                ? primaryKey.asc()
                : primaryKey.desc();

        Result<R> results = query.and(primaryKeyCondition)
                .orderBy(sortField)
                .limit(limit + 2)
                .fetch();

        int toSkip = 0;

        if (results.isNotEmpty() && results.get(0).getValue(primaryKey).equals(start)) {
            toSkip++;
        }

        List<R> sortedResults = results.stream()
                .skip(toSkip)
                .limit(limit)
                .sorted(Comparator.comparing(record -> record.getValue(primaryKey), sort))
                .collect(Collectors.toList());

        K left = null;
        K right = null;

        if (!sortedResults.isEmpty()) {
            left = sortedResults.get(0).getValue(primaryKey);
            right = sortedResults.get(sortedResults.size() - 1).getValue(primaryKey);
        }

        boolean hasPredecessors = !results.isEmpty() && results.get(0).getValue(primaryKey).equals(start);
        boolean hasSuccessors = results.size() == (limit + 2);

        if (next) {
            return new Range<>(sortedResults, left, right, hasPredecessors, hasSuccessors);
        } else {
            return new Range<>(sortedResults, left, right, hasSuccessors, hasPredecessors);
        }
    }
}
