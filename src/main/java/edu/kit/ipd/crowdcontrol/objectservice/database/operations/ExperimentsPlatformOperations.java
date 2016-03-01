package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.google.common.collect.Sets;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformModeStopgap;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformModeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformStatusRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.DSL;

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
     * @throws IllegalArgumentException if the platform is already existing
     */
    public void insertPlatform(String platform, int experimentId, ExperimentsPlatformModeStopgap mode) throws IllegalArgumentException {
        ExperimentsPlatformRecord record = new ExperimentsPlatformRecord(null, experimentId, platform, null);
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
     * Stores the ExperimentsPlatforms in the db and deletes the one not existing.
     * All newly inserts Platforms will be inserted with the status draft.
     * @param platforms the names of the platforms
     * @param experimentId the primary key of the experiment
     */
    public void storePlatforms(List<String> platforms, int experimentId) {
        Set<ExperimentsPlatformRecord> toStore = platforms.stream()
                .map(platform -> new ExperimentsPlatformRecord(null, experimentId, platform, null))
                .collect(Collectors.toSet());

        List<ExperimentsPlatformRecord> inserted = create.transactionResult(conf -> {
            Set<String> existing = DSL.using(conf).select(EXPERIMENTS_PLATFORM.PLATFORM)
                    .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                    .fetchSet(EXPERIMENTS_PLATFORM.PLATFORM);

            List<ExperimentsPlatformRecord> toInsert = toStore.stream()
                    .filter(record -> !existing.contains(record.getPlatform()))
                    .collect(Collectors.toList());

            existing.removeAll(platforms);

            DSL.using(conf).batchInsert(toInsert).execute();

            DSL.using(conf).deleteFrom(EXPERIMENTS_PLATFORM)
                    .where(EXPERIMENTS_PLATFORM.PLATFORM.in(existing))
                    .and(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                    .execute();

            return toInsert;
        });

        List<String> platformsInserted = inserted.stream()
                .map(ExperimentsPlatformRecord::getPlatform)
                .collect(Collectors.toList());

        Result<ExperimentsPlatformRecord> inTheDatabase = create.selectFrom(EXPERIMENTS_PLATFORM)
                .where(EXPERIMENTS_PLATFORM.EXPERIMENT.eq(experimentId))
                .and(EXPERIMENTS_PLATFORM.PLATFORM.in(platformsInserted))
                .fetch();

        List<ExperimentsPlatformStatusRecord> statusesToInsert = inTheDatabase.stream()
                .map(record -> new ExperimentsPlatformStatusRecord(
                        null,
                        ExperimentsPlatformStatusPlatformStatus.draft,
                        null,
                        record.getIdexperimentsPlatforms()
                ))
                .collect(Collectors.toList());

        create.batchInsert(statusesToInsert).execute();
    }

    /**
     * sets the passed platform to the passed mode
     * @param platform the platform to set the mode to
     * @param experiment the primary key of the experiment
     * @param mode the mode to set the platforms to
     */
    public void setPlatformMode(String platform, int experiment, ExperimentsPlatformModeStopgap mode) {
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

        ExperimentsPlatformModeRecord toInsert = new ExperimentsPlatformModeRecord(null, experimentsPlatform, mode, null);

        create.batchInsert(toInsert).execute();
    }

    /**
     * sets the mode for the passed ExperimentsPlatform
     * @param experimentsPlatform the primary key of the ExperimentsPlatform
     * @param mode the mode to set to
     */
    public void setPlatformMode(int experimentsPlatform, ExperimentsPlatformModeStopgap mode) {
        ExperimentsPlatformModeRecord toInsert = new ExperimentsPlatformModeRecord(null, experimentsPlatform, mode, null);

        create.executeInsert(toInsert);
    }

    /**
     * sets the mode for the passed status
     * @param experimentsPlatform the primary key of the ExperimentsPlatform
     * @param status the status to set to
     */
    public void setPlatformStatus(int experimentsPlatform, ExperimentsPlatformStatusPlatformStatus status) {
        ExperimentsPlatformStatusRecord toInsert = new ExperimentsPlatformStatusRecord(null, status, null, experimentsPlatform);

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
}
