package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import org.jooq.DSLContext;

/**
 * @author LeanderK
 * @version 1.0
 */
public abstract class AbstractOperation {
    protected final DSLContext create;

    protected AbstractOperation(DSLContext create) {
        this.create = create;
    }
}
