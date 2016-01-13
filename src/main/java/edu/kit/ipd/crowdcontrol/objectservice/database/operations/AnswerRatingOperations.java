package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import org.jooq.DSLContext;

/**
 * responsible for all queries related to the Answer and Rating Table
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingOperations extends AbstractOperation {
    protected AnswerRatingOperations(DSLContext create) {
        super(create);
    }
}
