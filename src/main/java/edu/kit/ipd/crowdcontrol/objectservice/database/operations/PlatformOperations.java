package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Platform;
import org.jooq.DSLContext;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.PLATFORM;

/**
 * the Operations concerned with the Platform-Table.
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
        return getNextRange(create.selectFrom(PLATFORM), PLATFORM.ID_PLATFORM, cursor, next, limit, String::compareTo)
                .map(this::toProto);
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
                .map(this::toProto);
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
     * Delete all platforms existing in the database.
     */
    public void deleteAllPlatforms() {
        create.deleteFrom(Tables.PLATFORM).execute();
    }

    private Platform toProto(PlatformRecord record) {
        return Platform.newBuilder()
                .setId(record.getIdPlatform())
                .setName(record.getName())
                .build();
    }
}
