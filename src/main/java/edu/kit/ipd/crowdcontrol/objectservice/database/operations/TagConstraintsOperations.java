package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;

/**
 * Created by marcel on 27.01.16.
 */
public class TagConstraintsOperations extends AbstractOperations {
    protected TagConstraintsOperations(DSLContext create) {
        super(create);
    }

    public TagRecord createTag(TagRecord tagRecord) {
        //TODO leander
        return null;
    }
    public ConstraintRecord createConstraint(ConstraintRecord constraintRecord) {
        //TODO leander
        return null;
    }

    public List<TagRecord> getTags(int experimentId) {
        return null;
    }

    public List<ConstraintRecord> getConstraints(int experimentId) {
        return null;
    }
}
