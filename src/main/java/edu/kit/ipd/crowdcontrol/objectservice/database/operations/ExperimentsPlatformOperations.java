package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformModeMode;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ExperimentsPlatformMode;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ExperimentsPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformModeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformStatusRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Result;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * Responsible for the operations involving the creation of tasks.
 * @author LeanderK
 * @version 1.0
 */
public class ExperimentsPlatformOperations extends AbstractOperations {
    public ExperimentsPlatformOperations(DSLContext create) {
        super(create);
    }

    /**
     * Inserts the passed platform into the database with the status draft
     * @param platform the name of the platform
     * @param experimentId the primary key of the experiment
     * @param mode the mode of the platform
     * @throws IllegalArgumentException if the platform is already existing
     */
    public void insertPlatform(String platform, int experimentId, ExperimentsPlatformModeMode mode) throws IllegalArgumentException {
        ExperimentsPlatformRecord record = new ExperimentsPlatformRecord();
        record.setExperiment(experimentId);
        record.setPlatform(platform);
        ExperimentsPlatformRecord inserted = create.transactionResult(conf -> {
            boolean exists = DSL.using(conf).fetchExists(
                    DSL.selectFrom(EXPERIMENTS_PLATFORM)
                            .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                            .and(EXPERIMENTS_PLATFORM.PLATFORM.eq(platform))
            );
            if (!exists) {
                return Optional.of(create.insertInto(EXPERIMENTS_PLATFORM)
                        .set(record)
                        .returning()
                        .fetchOne());
            } else {
                return Optional.<ExperimentsPlatformRecord>empty();
            }
        }).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Platform %s is already existing for experiment %d.",
                platform,
                experimentId
        )));

        setPlatformStatus(inserted.getIdexperimentsPlatforms(), ExperimentsPlatformStatusPlatformStatus.draft);

        setPlatformMode(inserted.getIdexperimentsPlatforms(), mode);
    }

    /**
     * stores the platforms for the passed experiment in the Experiments_Platform-Tables
     * @param platforms the platforms to store
     * @param experimentId the primary key of the experiment
     */
    public void storeExperimentsPlatforms(List<String> platforms, int experimentId) {
        create.transaction(conf -> {
            DSL.using(conf).deleteFrom(EXPERIMENTS_PLATFORM)
                    .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                    .and(EXPERIMENTS_PLATFORM.PLATFORM.notIn(platforms))
                    .execute();

            Set<String> existing = DSL.using(conf).select(EXPERIMENTS_PLATFORM.PLATFORM)
                    .from(EXPERIMENTS_PLATFORM)
                    .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                    .fetch()
                    .intoSet(EXPERIMENTS_PLATFORM.PLATFORM);

            List<ExperimentsPlatformRecord> toInsert = platforms.stream()
                    .filter(platform -> !existing.contains(platform))
                    .map(platform -> {
                        ExperimentsPlatformRecord record = new ExperimentsPlatformRecord();
                        record.setExperiment(experimentId);
                        record.setPlatform(platform);
                        return record;
                    })
                    .collect(Collectors.toList());

            DSL.using(conf).batchInsert(toInsert).execute();
        });
    }

    /**
     * stores all the modes for the platform as long as the experimentsPlatform is existing
     * @param statuses the modes to store
     * @param experiment the primary key of the experiment
     */
    public void storeExperimentsModes(Map<String, ExperimentsPlatformModeMode> statuses, int experiment) {
        Map<String, Integer> platfromIDMap = create.select(EXPERIMENTS_PLATFORM.PLATFORM, EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                .from(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experiment))
                .fetchMap(EXPERIMENTS_PLATFORM.PLATFORM, EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS);

        ExperimentsPlatformMode maxDateJoin = EXPERIMENTS_PLATFORM_MODE.as("maxDateJoin");
        Field<Timestamp> maxDate = DSL.max(maxDateJoin.field(EXPERIMENTS_PLATFORM_MODE.TIMESTAMP)).as("maxDate");
        Field<Integer> maxDateJoinPlatform = maxDateJoin.field(EXPERIMENTS_PLATFORM_MODE.EXPERIMENTS_PLATFORM).as("maxDatePlatform");
        Map<Integer, ExperimentsPlatformModeMode> existingModes = create
                .select(EXPERIMENTS_PLATFORM_MODE.MODE, EXPERIMENTS_PLATFORM_MODE.EXPERIMENTS_PLATFORM, EXPERIMENTS_PLATFORM_MODE.TIMESTAMP)
                .from(EXPERIMENTS_PLATFORM_MODE)
                .innerJoin(
                        DSL.select(maxDateJoinPlatform, maxDate)
                                .from(maxDateJoin)
                                .groupBy(maxDateJoinPlatform)
                ).on(EXPERIMENTS_PLATFORM_MODE.EXPERIMENTS_PLATFORM.eq(maxDateJoinPlatform)
                        .and(EXPERIMENTS_PLATFORM_MODE.TIMESTAMP.eq(maxDate)))
                .where(EXPERIMENTS_PLATFORM_MODE.EXPERIMENTS_PLATFORM.in(
                        DSL.select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                                .from(EXPERIMENTS_PLATFORM)
                                .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(1))
                ))
                .fetchMap(EXPERIMENTS_PLATFORM_MODE.EXPERIMENTS_PLATFORM, EXPERIMENTS_PLATFORM_MODE.MODE);

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        List<ExperimentsPlatformModeRecord> toInsert = statuses.entrySet().stream()
                .filter(entry -> platfromIDMap.containsKey(entry.getKey()))
                .map(entry -> new ExperimentsPlatformModeRecord(null, platfromIDMap.get(entry.getKey()), entry.getValue(), timestamp))
                .filter(record -> !record.getMode().equals(existingModes.get(record.getExperimentsPlatform())))
                .collect(Collectors.toList());

        create.batchInsert(toInsert).execute();
    }

    /**
     * sets the passed platform to the passed mode
     * @param platform the platform to set the mode to
     * @param experiment the primary key of the experiment
     * @param mode the mode to set the platforms to
     */
    public void setPlatformMode(String platform, int experiment, ExperimentsPlatformModeMode mode) {
        Integer experimentsPlatform = create.select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                .from(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.PLATFORM.eq(platform))
                .and(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experiment))
                .fetchOptional()
                .map(Record1::value1)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "platform %s is not existing for experiment %d",
                        platform,
                        experiment
                )));

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        ExperimentsPlatformModeRecord toInsert = new ExperimentsPlatformModeRecord(null, experimentsPlatform, mode, timestamp);

        create.batchInsert(toInsert).execute();
    }

    /**
     * sets the mode for the passed ExperimentsPlatform
     * @param experimentsPlatform the primary key of the ExperimentsPlatform
     * @param mode the mode to set to
     */
    public void setPlatformMode(int experimentsPlatform, ExperimentsPlatformModeMode mode) {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        ExperimentsPlatformModeRecord toInsert = new ExperimentsPlatformModeRecord(null, experimentsPlatform, mode, timestamp);

        create.executeInsert(toInsert);
    }

    /**
     * sets the platform to the passed status
     * @param experimentsPlatform the primary key of the ExperimentsPlatform
     * @param status the status to set to
     */
    public void setPlatformStatus(int experimentsPlatform, ExperimentsPlatformStatusPlatformStatus status) {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        ExperimentsPlatformStatusRecord toInsert = new ExperimentsPlatformStatusRecord(null, status, timestamp, experimentsPlatform);

        create.executeInsert(toInsert);
    }

    /**
     * sets the platform to the passed status
     * @param experiment the primary key of the experiment
     * @param platform the platform
     * @param status the to set the platform to
     */
    public void setPlatformStatus(int experiment, String platform, ExperimentsPlatformStatusPlatformStatus status) {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        Integer idExperimentsPlatforms = getExperimentsPlatform(platform, experiment)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "ExperimentsPlatform is not existing for platform %s and experiment %d",
                        platform,
                        experiment)))
                .getIdexperimentsPlatforms();
        ExperimentsPlatformStatusRecord toInsert = new ExperimentsPlatformStatusRecord(null, status, timestamp, idExperimentsPlatforms);

        create.executeInsert(toInsert);
    }

     /**
     * Updates a ExperimentsPlatform.
     * @param platformRecord the update
     * @return whether the update was successful
     * @throws IllegalArgumentException if the record has no primary key
     */
    public boolean updateExperimentsPlatform(ExperimentsPlatformRecord platformRecord) throws IllegalArgumentException {
        assertHasPrimaryKey(platformRecord);
        return create.executeUpdate(platformRecord) == 1;
    }

    /**
     * Searches for a task specified by platform and experimentId.
     * @param platform the string of the platform
     * @param experimentId the primary key of the experiment
     * @return the found task or empty if not found
     */
    public Optional<ExperimentsPlatformRecord> getExperimentsPlatform(String platform, int experimentId) {
        return create.selectFrom(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.PLATFORM.eq(platform))
                .and(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                .fetchOptional();
    }

    /**
     * gets all the active platform for the experiment
     * @param experimentId the primary key of the experiment
     * @return a list of platforms
     */
    public Map<String, ExperimentsPlatformModeMode> getActivePlatforms(int experimentId) {
        ExperimentsPlatformMode mode1 = EXPERIMENTS_PLATFORM_MODE.as("mode1");
        ExperimentsPlatformMode mode2 = EXPERIMENTS_PLATFORM_MODE.as("mode2");
        return create.select(EXPERIMENTS_PLATFORM.PLATFORM, mode1.MODE)
                .from(EXPERIMENTS_PLATFORM)
                .join(mode1).onKey()
                .leftOuterJoin(mode2).on(
                        EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS.eq(mode2.EXPERIMENTS_PLATFORM)
                        .and(mode1.TIMESTAMP.lessThan(mode2.TIMESTAMP).or(mode1.TIMESTAMP.eq(mode2.TIMESTAMP)
                                .and(mode1.IDEXPERIMENTS_PLATFORM_STOPGAP.lessThan(mode2.IDEXPERIMENTS_PLATFORM_STOPGAP))))
                )
                .where(mode2.IDEXPERIMENTS_PLATFORM_STOPGAP.isNull())
                .and(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                .fetchMap(EXPERIMENTS_PLATFORM.PLATFORM, EXPERIMENTS_PLATFORM_MODE.MODE);
    }

    /**
     * Deletes a TaskRecord matching the primary key of the passed TaskRecord
     * @param experimentsPlatform the primary key of the ExperimentsPlatform to delete
     */
    public void deleteExperimentsPlatform(int experimentsPlatform) {
        create.deleteFrom(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS.eq(experimentsPlatform))
                .execute();
    }

    /**
     * Returns all the platforms for the experiment.
     * @param experimentId the primary key of the experiment
     * @return a list of all ExperimentsPlatforms
     */
    public List<ExperimentsPlatformRecord> getExperimentPlatforms(int experimentId) {
        return create.selectFrom(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                .fetch();
    }

    /**
     * returns the latest statuses for all platforms
     * @param experiment the experiment to get the statuses for
     * @return the statuses
     */
    public Set<ExperimentsPlatformStatusPlatformStatus> getExperimentsPlatformStatusPlatformStatuses (int experiment) {
        ExperimentsPlatformStatus status1 = EXPERIMENTS_PLATFORM_STATUS.as("mode1");
        ExperimentsPlatformStatus status2 = EXPERIMENTS_PLATFORM_STATUS.as("mode2");
        return create.select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS, status1.PLATFORM_STATUS)
                .from(EXPERIMENTS_PLATFORM)
                .join(status1).onKey()
                .leftOuterJoin(status2).on(
                        EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS.eq(status2.PLATFORM)
                                .and(status1.TIMESTAMP.lessThan(status2.TIMESTAMP).or(status1.TIMESTAMP.eq(status2.TIMESTAMP)
                                        .and(status1.IDEXPERIMENTS_PLATFORM_STATUS.lessThan(status2.IDEXPERIMENTS_PLATFORM_STATUS))))
                )
                .where(status2.IDEXPERIMENTS_PLATFORM_STATUS.isNull())
                .and(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experiment))
                .fetchSet(status1.PLATFORM_STATUS);

    }

    /**
     * Updates the status of all platforms of specified experiment to given status
     * @param experiment the experiment whose platforms will be updated
     * @param globalPlatformStatus the new platform-status
     */
    public void setGlobalPlatformStatus(Experiment experiment, ExperimentsPlatformStatusPlatformStatus globalPlatformStatus) {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

        List<ExperimentsPlatformStatusRecord> toInsert = create.select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                .from(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experiment.getId()))
                .fetch()
                .map(record -> new ExperimentsPlatformStatusRecord(null, globalPlatformStatus, timestamp, record.value1()));

        create.batchInsert(toInsert).execute();
    }

    /**
     * Retrieves the ExperimentsPlatformStatusRecord of specified experiment which contains information about the
     * current status of the platforms and the timestamp of the last change
     * @param experimentID the experiment
     * @return ExperimentsPlatformStatusRecord, containing information about status and status-change-time
     */
    public List<ExperimentsPlatformStatusRecord> getExperimentsPlatformStatusRecord(int experimentID) {
        return create.selectFrom(EXPERIMENTS_PLATFORM_STATUS)
                .where(EXPERIMENTS_PLATFORM_STATUS.PLATFORM.in(
                        DSL.select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS)
                            .from(EXPERIMENTS_PLATFORM)
                            .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentID))
                ))
                .fetch();
    }


    /**
     * Retrieves a list of all experiments containing the shutdown-state and which didn't
     * reach the state-stopped yet.
     * @return list of experiments to recover
     */
    public List<ExperimentRecord> getExperimentsFailedDuringShutdown() {
        ExperimentsPlatformStatus status1 = EXPERIMENTS_PLATFORM_STATUS.as("mode1");
        ExperimentsPlatformStatus status2 = EXPERIMENTS_PLATFORM_STATUS.as("mode2");
        Set<Integer> experimentsPlatforms = create.select(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS, status1.PLATFORM_STATUS)
                .from(EXPERIMENTS_PLATFORM)
                .join(status1).onKey()
                .leftOuterJoin(status2).on(
                        EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS.eq(status2.PLATFORM)
                                .and(status1.TIMESTAMP.lessThan(status2.TIMESTAMP).or(status1.TIMESTAMP.eq(status2.TIMESTAMP)
                                        .and(status1.IDEXPERIMENTS_PLATFORM_STATUS.lessThan(status2.IDEXPERIMENTS_PLATFORM_STATUS))))
                )
                .where(status2.IDEXPERIMENTS_PLATFORM_STATUS.isNull())
                .having(status1.PLATFORM_STATUS.eq(ExperimentsPlatformStatusPlatformStatus.shutdown))
                .fetchSet(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS);

        return create.selectFrom(EXPERIMENT)
                .where(EXPERIMENT.ID_EXPERIMENT.in(
                        DSL.select(EXPERIMENTS_PLATFORM.EXPERIMENT)
                            .from(EXPERIMENTS_PLATFORM)
                            .where(EXPERIMENTS_PLATFORM.IDEXPERIMENTS_PLATFORMS.in(
                                    experimentsPlatforms
                            ))
                ))
                .fetch();
    }

    /**
     * Retrieves a list of all experiments which are in the state running, creative_stopped, shutdown
     */
    public List<ExperimentRecord> getRunningExperiments() {
        Predicate<Integer> isRunningStoppedOrShutdown = experimentID -> {
            Set<ExperimentsPlatformStatusPlatformStatus> statuses = getExperimentsPlatformStatusRecord(experimentID).stream()
                    .map(ExperimentsPlatformStatusRecord::getPlatformStatus)
                    .collect(Collectors.toSet());
            return statuses.contains(ExperimentsPlatformStatusPlatformStatus.running)
                    || statuses.contains(ExperimentsPlatformStatusPlatformStatus.shutdown)
                    || statuses.contains(ExperimentsPlatformStatusPlatformStatus.creative_stopping);
        };
        return create.selectFrom(EXPERIMENT)
                .fetch()
                .stream()
                .filter(experiment -> isRunningStoppedOrShutdown.apply(experiment.getIdExperiment()))
                .collect(Collectors.toList());
    }

    /**
     * returns all the identifications in the Experiments_Platform table where the name is the platform
     * @param platform the platform to search for
     * @return a list of identifications
     */
    public List<String> getIdentificationsfromPlatform(String platform) {
        return create.select(EXPERIMENTS_PLATFORM.IDENTIFICATION)
                .from(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.IDENTIFICATION.isNotNull())
                .and(EXPERIMENTS_PLATFORM.PLATFORM.eq(platform))
                .fetch(EXPERIMENTS_PLATFORM.IDENTIFICATION);
    }
}
