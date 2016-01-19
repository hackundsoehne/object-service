package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import org.jooq.DSLContext;

/**
 * Created by marcel on 19.01.16.
 */
public class PlatformOperations extends AbstractOperations {
    protected PlatformOperations(DSLContext create) {
        super(create);
    }

    /**
     * Insert new platform
     * @param taskRecord The platform
     * @return
     */
    public int createPlatform(PlatformRecord taskRecord) {

    }

    /**
     * Delete all platforms
     */
    public void deleteAllPlatform() {

    }
}
