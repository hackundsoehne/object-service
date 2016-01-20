package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import org.jooq.DSLContext;

/**
 * the Operations concerned with the Platform-Table.
 * @author LeanderK
 * @author Marcel Hollerbach
 */
public class PlatformOperations extends AbstractOperations {
    protected PlatformOperations(DSLContext create) {
        super(create);
    }

    /**
     * Insert new platform into the database
     * @param platformRecord The platform to insert
     * @return true if inserted, false if existing
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
}
