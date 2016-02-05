package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.dummy.DummyPlatform;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.PlatformTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Platform;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * @return List of platforms.
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
     * @return The platform.
     */
    public Optional<Platform> getPlatform(String id) {
        return create.fetchOptional(PLATFORM, PLATFORM.ID_PLATFORM.eq(id))
                .map(PlatformTransformer::toProto);
    }

    /**
     * Insert new platform into the database
     *
     * @param platformRecord The platform to insert
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
     * Update the record of the given id with the new values
     * @param rec A new Platformrecord
     */
    public void updatePlatform(PlatformRecord rec) {
        //TODO LEAAAAANDER
    }

    /**
     * Get a List of all Platforms
     * @return List containing all platforms which are currently in the database
     */
    public List<PlatformRecord> getPlatforms() {
        //TODO LEAAAAANDER
        return Collections.emptyList();
    }
}
