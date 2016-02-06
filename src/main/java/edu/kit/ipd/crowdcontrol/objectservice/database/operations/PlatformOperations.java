package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.PlatformTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Platform;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.PLATFORM;

/**
 * Operations concerned with the {@code Platform} table.
 *
 * @author LeanderK
 * @author Marcel Hollerbach
 * @author Niklas Keller
 */
public class PlatformOperations extends AbstractOperations {
    public PlatformOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of platforms starting from {@code cursor}.
     *
     * @param cursor pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  number of records
     *
     * @return list of platforms.
     */
    public Range<Platform, String> getPlatformList(String cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(PLATFORM), PLATFORM.ID_PLATFORM, PLATFORM, cursor, next, limit, String::compareTo)
                .map(PlatformTransformer::toProto);
    }

    /**
     * Returns a single platform.
     *
     * @param id ID of the platform
     *
     * @return the platform.
     */
    public Optional<Platform> getPlatform(String id) {
        return create.fetchOptional(PLATFORM, PLATFORM.ID_PLATFORM.eq(id))
                .map(PlatformTransformer::toProto);
    }

    /**
     * Insert new platform into the database
     *
     * @param platformRecord the platform to insert
     *
     * @return true if inserted, false if existing
     *
     * @throws IllegalArgumentException if the record has no primary key
     */
    public boolean createPlatform(PlatformRecord platformRecord) throws IllegalArgumentException {
        assertHasPrimaryKey(platformRecord);
        return create.executeInsert(platformRecord) == 1;
    }

    /**
     * Stores the passed PlatformRecords.
     * <p>
     * updates all the Records matching the primary key, and sets every other record in the database to inactive.
     * @param toStore the records to store
     */
    public void storePlatforms(List<PlatformRecord> toStore) {
        toStore.forEach(this::assertHasPrimaryKey);
        toStore.forEach(record -> assertHasField(record, PLATFORM.NAME, PLATFORM.NEEDS_EMAIL, PLATFORM.RENDER_CALIBRATIONS));
        List<String> primaryKeys = toStore.stream().map(PlatformRecord::getIdPlatform).collect(Collectors.toList());
        create.transaction(conf -> {
            DSL.using(conf).batchStore(toStore);
            DSL.using(conf).update(PLATFORM)
                    .set(PLATFORM.INACTIVE, true)
                    .where(PLATFORM.ID_PLATFORM.notIn(primaryKeys))
                    .execute();
        });
    }
}
