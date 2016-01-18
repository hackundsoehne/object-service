package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
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
     * @param id the id of the event
     * @param function the function to execute
     * @param <R> the return type
     * @return the result of the function
     */
    protected <R> R doIfNotRunning(int id, Function<Configuration, R> function) {
        return create.transactionResult(trans -> {
            int running = DSL.using(trans)
                    .fetchCount(
                            Tables.TASK,
                            Tables.TASK.EXPERIMENT.eq(id).and(Tables.TASK.RUNNING.isTrue()));
            if (running == 0) {
                return function.apply(trans);
            } else {
                //TODO other exception?
                throw new IllegalArgumentException("Experiment is running");
            }
        });
    }

    protected <R extends org.jooq.Record> Range<R> getRange(SelectWhereStep<R> step, Field<Integer> primaryKey,
                                                            int from, boolean next, int limit) {
        return getRange(step.where(true), primaryKey, from, next, limit);
    }

    protected <R extends org.jooq.Record> Range<R> getRange(SelectConditionStep<R> step, Field<Integer> primaryKey,
                                                            int from, boolean next, int limit) {
        Condition primaryKeyCondition = next
                            ? primaryKey.greaterOrEqual(from)
                            : primaryKey.lessThan(from);

        SortField<Integer> sortField = next
                ? primaryKey.asc()
                : primaryKey.desc();

        Result<R> results = step.and(primaryKeyCondition)
                .orderBy(sortField)
                .limit(limit + 1)
                .fetch();

        boolean hasMore = results.size() == (limit + 1);

        List<R> sortedResults = results.stream()
                .limit(limit)
                .sorted(Comparator.comparingInt(record -> record.getValue(primaryKey)))
                .collect(Collectors.toList());

        return new Range<>(sortedResults, hasMore);
    }
}
