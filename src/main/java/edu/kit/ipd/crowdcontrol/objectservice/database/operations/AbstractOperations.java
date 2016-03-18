package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.google.protobuf.Descriptors;
import com.google.protobuf.MessageOrBuilder;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ExperimentsPlatformStatus;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableRecordImpl;

import java.util.*;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.EXPERIMENTS_PLATFORM;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.EXPERIMENTS_PLATFORM_STATUS;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus.running;

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
     * executes the function if the experiment is in draft-mode.
     * @param experimentID the id of the experiment
     * @param function the function to execute
     * @param <R> the return type
     * @return the result of the function
     * @throws IllegalStateException if the experiment is running
     */
    protected <R> R doIfDraft(int experimentID, Function<Configuration, R> function) throws IllegalStateException {
        return create.transactionResult(trans -> {
            boolean notDraft = DSL.using(trans).fetchExists(
                    DSL.selectFrom(Tables.EXPERIMENTS_PLATFORM_STATUS)
                            .where(Tables.EXPERIMENTS_PLATFORM_STATUS.PLATFORM.in(
                                    DSL.select(Tables.EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                                            .from(Tables.EXPERIMENTS_PLATFORM)
                                            .where(Tables.EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentID))
                            ))
                            .and(Tables.EXPERIMENTS_PLATFORM_STATUS.PLATFORM_STATUS
                                    .notEqual(ExperimentsPlatformStatusPlatformStatus.draft))
            );
            if (!notDraft) {
                return function.apply(trans);
            } else {
                throw new IllegalStateException("Experiment is not in draft: " + experimentID);
            }
        });
    }

    /**
     * executes the function if the experiment is running.
     * @param experimentID the id of the experiment
     * @param function the function to execute
     * @param <R> the return type
     * @return the result of the function
     * @throws IllegalStateException if the experiment is not running
     */
    protected <R> R doIfRunning(int experimentID, Function<Configuration, R> function) throws IllegalStateException {
        boolean running = create.fetchExists(
                DSL.selectFrom(Tables.EXPERIMENTS_PLATFORM_STATUS)
                        .where(Tables.EXPERIMENTS_PLATFORM_STATUS.PLATFORM.in(
                                DSL.select(Tables.EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                                        .from(Tables.EXPERIMENTS_PLATFORM)
                                        .where(Tables.EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentID))
                        ))
                        .and(Tables.EXPERIMENTS_PLATFORM_STATUS.PLATFORM_STATUS
                                .eq(ExperimentsPlatformStatusPlatformStatus.running)
                                .or(Tables.EXPERIMENTS_PLATFORM_STATUS.PLATFORM_STATUS
                                    .eq(ExperimentsPlatformStatusPlatformStatus.creative_stopping))
                                .or(Tables.EXPERIMENTS_PLATFORM_STATUS.PLATFORM_STATUS
                                    .eq(ExperimentsPlatformStatusPlatformStatus.shutdown))
                        )

        );
        if (running) {
            return create.transactionResult(function::apply);
        } else {
            throw new IllegalStateException("Experiment is not running: " + experimentID);
        }
    }

    /**
     * executes the function if the experiment is not running.
     * @param experimentID the id of the experiment
     * @param function the function to execute
     * @param <R> the return type
     * @return the result of the function
     * @throws IllegalStateException if the experiment is running
     */
    protected <R> R doIfNotRunning(int experimentID, Function<Configuration, R> function) throws IllegalStateException {
        return create.transactionResult(trans -> {
            ExperimentsPlatformStatus status1 = EXPERIMENTS_PLATFORM_STATUS.as("mode1");
            ExperimentsPlatformStatus status2 = EXPERIMENTS_PLATFORM_STATUS.as("mode2");
            Set<ExperimentsPlatformStatusPlatformStatus> statuses = DSL.using(trans).select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS, status1.PLATFORM_STATUS)
                    .from(EXPERIMENTS_PLATFORM)
                    .join(status1).onKey()
                    .leftOuterJoin(status2).on(
                            EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS.eq(status2.PLATFORM)
                                    .and(status1.TIMESTAMP.lessThan(status2.TIMESTAMP).or(status1.TIMESTAMP.eq(status2.TIMESTAMP)
                                            .and(status1.IDEXPERIMENTS_PLATFORM_STATUS.lessThan(status2.IDEXPERIMENTS_PLATFORM_STATUS))))
                    )
                    .where(status2.IDEXPERIMENTS_PLATFORM_STATUS.isNull())
                    .and(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentID))
                    .fetchSet(status1.PLATFORM_STATUS);
            if (statuses.contains(ExperimentsPlatformStatusPlatformStatus.running)
                    || statuses.contains(ExperimentsPlatformStatusPlatformStatus.creative_stopping)
                    || statuses.contains(ExperimentsPlatformStatusPlatformStatus.shutdown)) {
                throw new IllegalStateException("Experiment is running: " + experimentID);
            } else {
                return function.apply(trans);
            }
        });
    }

    /**
     * Throws an exception if the record has no primary key set.
     * @param record the record to check
     * @param <R> the type of he record.
     * @throws IllegalArgumentException thrown if the record has no primary key
     */
    protected <R extends TableRecord<R>> void assertHasPrimaryKey(TableRecordImpl<R> record) throws IllegalArgumentException {
        boolean hasPrimaryKey = record.getTable().getPrimaryKey().getFields().stream()
                .map(record::getValue)
                .filter(Objects::nonNull)
                .findAny()
                .isPresent();
        if (!hasPrimaryKey)
            throw new IllegalArgumentException("Record from Table: " + record.getTable().getName()
                    + " needs PrimaryKey for this action");
    }

    /**
     * Throws an exception if one of the passed field is not set.
     * @param record the record to check on
     * @param fields the fields to check for
     * @param <R> the type of the record
     * @throws IllegalArgumentException thrown if one of the fields is not set
     */
    protected <R extends TableRecord<R>> void assertHasField(TableRecordImpl<R> record, Field<?>... fields) throws IllegalArgumentException {
        if (Arrays.stream(fields).anyMatch(field -> record.getValue(field) == null))
            throw new IllegalArgumentException("Record from Table: " + record.getTable().getName()
                    + " needs PrimaryKey for this action");
    }

    /**
     * Throws an exception if the passed field is not set.
     * @param record the record to check on
     * @param field the field to check for
     * @param <R> the type of the record
     * @throws IllegalArgumentException thrown if the field is not set
     */
    protected <R extends TableRecord<R>> void assertHasField(TableRecordImpl<R> record, Field<?> field) throws IllegalArgumentException {
        if (record.getValue(field) == null)
            throw new IllegalArgumentException("Record from Table: " + record.getTable().getName()
                    + " needs PrimaryKey for this action");
    }

    /**
     * Throws an exception if the passed field is not set.
     * @param messageOrBuilder the MessageOrBuilder to check on
     * @param field the field to exist
     * @throws IllegalArgumentException thrown if the field is not set
     */
    protected void assertHasField(MessageOrBuilder messageOrBuilder, int field) throws IllegalArgumentException {
        Descriptors.FieldDescriptor fieldDescriptor = messageOrBuilder.getDescriptorForType().findFieldByNumber(field);
        if (!fieldDescriptor.isRepeated() && !messageOrBuilder.hasField(fieldDescriptor)) {
            throw new IllegalArgumentException("MessageOrBuilder must have field set: " +
                    fieldDescriptor.getName());
        } else if (fieldDescriptor.isRepeated() && ((List) messageOrBuilder.getField(fieldDescriptor)).isEmpty()) {
            throw new IllegalArgumentException("MessageOrBuilder must have non-empty field: " +
                    messageOrBuilder.getDescriptorForType().findFieldByNumber(field).getName());
        }
    }

    /**
     * Throws an exception if one of the passed field is not set.
     * @param messageOrBuilder the MessageOrBuilder to check on
     * @param fields the fields to exist
     * @throws IllegalArgumentException thrown if on the field is not set
     */
    protected void assertHasField(MessageOrBuilder messageOrBuilder, int... fields) throws IllegalArgumentException {
        for (int aField : fields) {
            assertHasField(messageOrBuilder, aField);
        }
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
     * @see #getNextRange(SelectWhereStep, Field, Table, Object, boolean, int, Comparator)
     */
    protected <R extends org.jooq.Record> Range<R, Integer> getNextRange(SelectWhereStep<R> query, Field<Integer> primaryKey, Table<?> tablePrimaryKey,
                                                                      Integer start, boolean next, int limit) {
        return getNextRange(query, primaryKey, tablePrimaryKey, start, next, limit, Comparator.naturalOrder());
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
     * @see #getNextRange(SelectWhereStep, Field, Table, Object, boolean, int, Comparator)
     */
    protected <R extends org.jooq.Record> Range<R, Integer> getNextRange(SelectConditionStep<R> query, Field<Integer> primaryKey, Table<?> tablePrimaryKey,
                                                                         Integer start, boolean next, int limit) {
        return getNextRange(query, primaryKey, tablePrimaryKey, start, next, limit, Comparator.naturalOrder());
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
     * @see #getNextRange(SelectWhereStep, Field, Table, Object, boolean, int, Comparator)
     */
    protected <R extends org.jooq.Record, K> Range<R, K> getNextRange(SelectWhereStep<R> query, Field<K> primaryKey, Table<?> tablePrimaryKey,
                                                            K start, boolean next, int limit, Comparator<K> sort) {
        return getNextRange(query.where(true), primaryKey, tablePrimaryKey, start, next, limit, sort);
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
    protected <R extends org.jooq.Record, K> Range<R, K> getNextRange(SelectConditionStep<R> query, Field<K> primaryKey, Table<?> tablePrimaryKey,
                                                            K start, boolean next, int limit, Comparator<K> sort) {
        //right now no support for joins with a 1 to n relationship
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
            toSkip = 1;
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

        boolean hasPredecessors = toSkip == 1;
        boolean hasSuccessors = results.size() > limit + toSkip;

        if (toSkip == 0) {
            primaryKeyCondition = next
                    ? primaryKey.lessThan(start)
                    : primaryKey.greaterThan(start);

            // FIXME: Ugly hack, otherwise we end up with id >= ? and id < ?
            Optional<R> before = query.and(false).or(primaryKeyCondition).limit(1).fetchOptional();
            hasPredecessors = before.isPresent();
        }

        if (next) {
            return Range.of(sortedResults, left, right, hasPredecessors, hasSuccessors);
        } else {
            return Range.of(sortedResults, left, right, hasSuccessors, hasPredecessors);
        }
    }

    //experimental, do not use in production!
    @SuppressWarnings("unused")
    private <R extends Record, K> Range<R, K> getNextRangeJoinExperimental(SelectConditionStep<R> query, Field<K> primaryKey,
                                                                           Table<?> tablePrimaryKey, K start, boolean next,
                                                                           int limit, Comparator<K> sort) {
        Condition primaryKeyCondition = next
                            ? primaryKey.greaterOrEqual(start)
                            : primaryKey.lessOrEqual(start);

        SortField<K> sortField = next
                ? primaryKey.asc()
                : primaryKey.desc();

        Field<Integer> f1 = DSL.field("@outerval:=@outerval+1", Integer.class);

        Table<Record1<K>> limit1 = DSL.select(primaryKey)
                .from(tablePrimaryKey)
                .where(primaryKeyCondition)
                .limit(limit + 2).asTable("limit");

        Field<K> limitPrim = limit1.field(primaryKey);

        query.getQuery().addJoin(limit1, JoinType.JOIN, primaryKey.eq(limitPrim));

        SelectSeekStep1<R, K> finalQuery = query
                .orderBy(sortField);
        Result<R> results = finalQuery
                .fetch();

        int toSkip = 0;

        if (results.isNotEmpty() && results.get(0).getValue(primaryKey).equals(start)) {
            toSkip++;
        }

        List<R> sortedResults = results.stream()
                .skip(toSkip)
                .sorted(Comparator.comparing(record -> record.getValue(primaryKey), sort))
                .collect(Collectors.toList());

        boolean hasPredecessors = !results.isEmpty() && results.get(0).getValue(primaryKey).equals(start);
        boolean hasSuccessors = results.size() == (limit + 2);

        if (hasPredecessors) {
            ListIterator<R> rListIterator = sortedResults.listIterator();
            while (rListIterator.hasNext()) {
                R nextValue = rListIterator.next();
                if (nextValue.getValue(primaryKey).equals(start)) {
                    rListIterator.remove();
                } else {
                    break;
                }
            }
        }

        if (hasSuccessors) {
            K lastPrimaryKey = sortedResults.get(sortedResults.size() - 1).getValue(primaryKey);
            for (int i = (sortedResults.size() - 1); i > 0; i--) {
                R nextValue = sortedResults.get(i);
                if (nextValue.getValue(primaryKey).equals(lastPrimaryKey)) {
                    sortedResults.remove(i);
                } else {
                    //finished
                    i = 0;
                }
            }

        }

        K left = null;
        K right = null;

        if (!sortedResults.isEmpty()) {
            left = sortedResults.get(0).getValue(primaryKey);
            right = sortedResults.get(sortedResults.size() - 1).getValue(primaryKey);
        }

        if (next) {
            return Range.of(sortedResults, left, right, hasPredecessors, hasSuccessors);
        } else {
            return Range.of(sortedResults, left, right, hasSuccessors, hasPredecessors);
        }
    }


}
