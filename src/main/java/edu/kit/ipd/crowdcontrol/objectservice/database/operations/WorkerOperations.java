package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.WorkerTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import org.apache.commons.validator.routines.EmailValidator;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * Responsible for the operations involving the worker-table.
 *
 * @author LeanderK
 * @author Niklas Keller
 */
public class WorkerOperations extends AbstractOperations {

    public WorkerOperations(DSLContext create) {
        super(create);
    }

    /**
     * inserts the WorkerRecord into the database.
     *
     * @param workerRecord the record to insert
     * @return the resulting WorkerRecord existing in the database
     */
    public WorkerRecord insertWorker(WorkerRecord workerRecord) {
        workerRecord.setIdWorker(null);
        workerRecord.setQuality(9);
        assertNonMalformedEmail(workerRecord.getEmail());

        return create.transactionResult(conf -> {
            boolean existing = DSL.using(conf).fetchExists(
                    DSL.selectFrom(Tables.WORKER)
                            .where(Tables.WORKER.PLATFORM.eq(workerRecord.getPlatform()))
                            .and(Tables.WORKER.IDENTIFICATION.eq(workerRecord.getIdentification()))
            );

            if (existing) {
                throw new IllegalArgumentException("worker with the same platform and identification is" +
                        "already existing");
            }

            return create.insertInto(Tables.WORKER)
                    .set(workerRecord)
                    .returning()
                    .fetchOne();
        });
    }

    /**
     * returns all the worker with the Credit-Balance greater or equal than the passed balance.
     * The workers then get sorted by their latest transaction, where the worker with the oldest latest transaction
     * comes first.
     * @param balance the balance to check for
     * @return a list of workers
     */
    public Result<WorkerRecord> getWorkerWithCreditBalanceGreaterOrEqual(int balance) {
        Field<BigDecimal> sum = DSL.sum(WORKER_BALANCE.TRANSACTION_VALUE).as("sum");
        AggregateFunction<Timestamp> latestTransaction = DSL.max(WORKER_BALANCE.TIMESTAMP);
        return create.select(sum)
                .from(WORKER_BALANCE)
                .join(WORKER).onKey()
                .where(sum.greaterOrEqual(new BigDecimal(balance)))
                .groupBy(WORKER.fields())
                .orderBy(latestTransaction.asc())
                .fetchInto(WORKER);
    }

    /**
     * Deletes the worker and assigns all his work to the anonymous worker.
     * <p>
     * The worker will be deleted, there is no way to pay him after this action.
     *
     * @param id the primary key of the worker to anonymize
     *
     * @throws IllegalArgumentException if the primary key is not set or the worker is not existing
     *                                  in the database
     */
    public void anonymizeWorker(int id) throws IllegalArgumentException {
        WorkerRecord toAnonymize = create.selectFrom(Tables.WORKER)
                .where(Tables.WORKER.ID_WORKER.eq(id))
                .fetchOptional()
                .orElseThrow(() -> new IllegalArgumentException("worker: " + id + " is not existing"));

        WorkerRecord anonWorker = create.transactionResult(configuration ->
                DSL.using(configuration).selectFrom(Tables.WORKER)
                        .where(Tables.WORKER.PLATFORM.eq(toAnonymize.getPlatform()))
                        .and(Tables.WORKER.IDENTIFICATION.eq("Anonymous Worker"))
                        .fetchOptional()
                        .orElseGet(() -> {
                            WorkerRecord record = new WorkerRecord();
                            record.setPlatformData(new JsonPrimitive("Anonymous Worker"));
                            record.setPlatform(toAnonymize.getPlatform());
                            return DSL.using(configuration).insertInto(Tables.WORKER)
                                    .set(record)
                                    .returning()
                                    .fetchOne();
                        }));
        create.transaction(conf -> {
            DSL.using(conf).deleteFrom(Tables.CALIBRATION_RESULT)
                    .where(Tables.CALIBRATION_RESULT.WORKER.eq(toAnonymize.getIdWorker()))
                    .execute();

            DSL.using(conf).update(Tables.ANSWER)
                    .set(Tables.ANSWER.WORKER_ID, anonWorker.getIdWorker())
                    .where(Tables.ANSWER.WORKER_ID.eq(toAnonymize.getIdWorker()))
                    .execute();

            DSL.using(conf).update(Tables.RATING)
                    .set(Tables.RATING.WORKER_ID, anonWorker.getIdWorker())
                    .where(Tables.ANSWER.WORKER_ID.eq(toAnonymize.getIdWorker()))
                    .execute();

            DSL.using(conf).update(Tables.WORKER_BALANCE)
                    .set(Tables.WORKER_BALANCE.WORKER, anonWorker.getIdWorker())
                    .where(Tables.WORKER_BALANCE.WORKER.eq(anonWorker.getIdWorker()))
                    .execute();

            DSL.using(conf).executeDelete(toAnonymize);
        });
    }

    /**
     * finds the worker with the passed platform and platform-identification data
     *
     * @param platform       the platform the wanted worker is working on
     * @param identification the platform-specific identification
     * @return the found worker or empty
     */
    public Optional<WorkerRecord> getWorker(String platform, String identification) {
        return create.selectFrom(Tables.WORKER)
                .where(Tables.WORKER.PLATFORM.eq(platform))
                .and(Tables.WORKER.IDENTIFICATION.eq(identification))
                .fetchOptional();
    }

    /**
     * finds the worker with the passed workerId in the database
     *
     * @param workerID the primary-key of the worker
     * @return the found worker or empty
     */
    public Optional<WorkerRecord> getWorker(int workerID) {
        return create.selectFrom(Tables.WORKER)
                .where(Tables.WORKER.ID_WORKER.eq(workerID))
                .fetchOptional();
    }

    /**
     * Returns all workers participating in the specified experiment.
     * @param expId Id of the experiment
     * @return a collection of all workers of the specified experiment
     */
    public Result<WorkerRecord> getWorkersOfExp(int expId){
        return create.selectFrom(WORKER)
                .where(WORKER.ID_WORKER.in(
                        DSL.select(ANSWER.WORKER_ID)
                                .from(ANSWER)
                                .where(ANSWER.EXPERIMENT.eq(expId))
                ))
                .or(WORKER.ID_WORKER.in(
                        DSL.select(RATING.WORKER_ID)
                                .from(RATING)
                                .where(RATING.EXPERIMENT.eq(expId))
                ))
                .fetch();
    }
    /**
     * Returns a single worker.
     *
     * @param id ID of the worker
     * @return the worker or empty if not found
     */
    public Optional<Worker> getWorkerProto(int id) {
        return create.fetchOptional(WORKER, WORKER.ID_WORKER.eq(id))
                .map(WorkerTransformer::toProto);
    }

    /**
     * Returns a range of workers starting from {@code cursor}.
     *
     * @param cursor Pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     * @return List of workers
     */
    public Range<Worker, Integer> getWorkersFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(WORKER), WORKER.ID_WORKER, WORKER, cursor, next, limit)
                .map(WorkerTransformer::toProto);
    }

    /**
     * Creates a new worker.
     *
     * @param toStore  worker to save
     * @param data the data of the worker
     * @return Worker with ID assigned
     * @throws IllegalArgumentException if the name or content is not set
     */
    public Worker insertWorker(Worker toStore, JsonElement data) {
        assertHasField(toStore, Worker.PLATFORM_FIELD_NUMBER);
        assertNonMalformedEmail(toStore.getEmail());

        WorkerRecord record = WorkerTransformer.mergeRecord(create.newRecord(WORKER), toStore);
        record.setPlatformData(data);
        record.store();

        return WorkerTransformer.toProto(record);
    }

    /**
     * Updates a worker
     *
     * @param toUpdate the worker to update
     * @param id the id of the worker
     * @return the updated Worker
     */
    public Worker updateWorker(Worker toUpdate, int id) {
        WorkerRecord workerRecord = WorkerTransformer.mergeRecord(create.newRecord(WORKER), toUpdate);
        workerRecord.setIdWorker(id);
        assertHasPrimaryKey(workerRecord);
        assertNonMalformedEmail(toUpdate.getEmail());

        boolean updated = create.update(WORKER)
                .set(workerRecord)
                .where(WORKER.ID_WORKER.eq(id))
                .execute() == 1;
        if (!updated) {
            throw new IllegalArgumentException(String.format("Worker %d is not existing", id));
        }
        return WorkerTransformer.toProto(getWorker(id)
                .orElseThrow(() -> new IllegalStateException("Database inconsistent")));
    }

    /**
     * Get Workers which gave at least one response on a experiment, from the given platform
     * @param experimentId experiment which was worked on
     * @param platformName name of the platform of the worker
     * @return A list of workerrecords
     */
    public List<WorkerRecord> getWorkerWithWork(int experimentId, String platformName) {
        Rating rating = RATING.as("rating");
        Answer answer = ANSWER.as("answer");
        return create.select(WORKER.fields())
                .select(rating.ID_RATING)
                .select(answer.ID_ANSWER)
                .from(ANSWER)
                .rightJoin(rating).on(
                        WORKER.ID_WORKER.eq(rating.WORKER_ID)
                                .and(rating.EXPERIMENT.eq(experimentId))
                )
                .rightJoin(answer).on(
                        WORKER.ID_WORKER.eq(answer.WORKER_ID)
                        .and(answer.EXPERIMENT.eq(experimentId))
                )
                .where(WORKER.PLATFORM.eq(platformName))
                .fetchInto(WORKER);
    }

    /**
     * validates that the email except the email is null or empty
     * @param email the email to validate
     * @throws IllegalArgumentException if the email is not valid
     */
    private void assertNonMalformedEmail(String email) throws IllegalArgumentException {
        if(email != null && !email.isEmpty() && !EmailValidator.getInstance(false).isValid(email)) {
            throw new IllegalArgumentException(String.format("email is not valid: %s", email));
        }
    }
}
