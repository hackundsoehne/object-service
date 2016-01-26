package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;

/**
 * Created by marcel on 26.01.16.
 */
public class ConstraintsTransformer {
    public static Constraint toProto(ConstraintRecord record) {
        return Constraint.newBuilder()
                .setName(record.getConstraint()).build();
    }
}
