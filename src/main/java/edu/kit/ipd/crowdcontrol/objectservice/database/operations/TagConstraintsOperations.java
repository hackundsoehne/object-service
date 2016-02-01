package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import org.jooq.DSLContext;

import java.util.List;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * contains all the queries needed to interact with the tables Tag and Constraint.
 * @author LeanderK
 * @author Marcel Hollerbach
 */
public class TagConstraintsOperations extends AbstractOperations {
    public TagConstraintsOperations(DSLContext create) {
        super(create);
    }

    /**
     * inserts a new Tag
     * @param tagRecord the TagRecord to insert
     * @return the resulting TagRecord
     */
    public TagRecord insertTag(TagRecord tagRecord) {
        return create.insertInto(TAG)
                .set(tagRecord)
                .returning()
                .fetchOne();
    }

    /**
     * inserts a new Constraint
     * @param constraintRecord the record to insert
     * @return the resulting record
     */
    public ConstraintRecord insertConstraint(ConstraintRecord constraintRecord) {
        return create.insertInto(CONSTRAINT)
                .set(constraintRecord)
                .returning()
                .fetchOne();
    }

    /**
     * returns all the tags for the experiment
     * @param experimentId the primary key of the experiment
     * @return a list of tags
     */
    public List<TagRecord> getTags(int experimentId) {
        return create.selectFrom(TAG)
                .where(TAG.EXPERIMENT.eq(experimentId))
                .fetch();
    }

    /**
     * returns all the constraints for the experiment
     * @param experimentId the primary key of the experiment
     * @return a list of constraints
     */
    public List<ConstraintRecord> getConstraints(int experimentId) {
        return create.selectFrom(CONSTRAINT)
                .where(CONSTRAINT.EXPERIMENT.eq(experimentId))
                .fetch();
    }

    /**
     * deletes all the tags for the passed experiment
     * @param experiment the primary of the experiment
     */
    public void deleteAllTags(int experiment) {
        create.deleteFrom(TAG)
                .where(TAG.EXPERIMENT.eq(experiment))
                .execute();
    }

    /**
     * deletes all the Constraints for the passed experiment
     * @param experiment the primary of the experiment
     */
    public void deleteAllConstraint(int experiment) {
        create.deleteFrom(CONSTRAINT)
                .where(CONSTRAINT.EXPERIMENT.eq(experiment))
                .execute();
    }
}
