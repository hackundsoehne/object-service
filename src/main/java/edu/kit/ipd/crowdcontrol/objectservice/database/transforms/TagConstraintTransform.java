package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by marcel on 26.01.16.
 */
public class TagConstraintTransform extends AbstractTransform {
    public static Tag toTagProto(TagRecord record) {
        return Tag.newBuilder()
                .setName(record.getTag()).build();
    }

    public static List<TagRecord> getTags(Experiment experiment) {
        if (experiment.getTagsCount() > 0)
            return experiment.getTagsList().stream().map(tag ->
                    new TagRecord(-1,
                            tag.getName(),
                            experiment.getId())).collect(Collectors.toList());
        return Collections.emptyList();
    }
    public static Constraint toContrainsProto(ConstraintRecord record) {
        return Constraint.newBuilder()
                .setName(record.getConstraint()).build();
    }
    public static List<ConstraintRecord> getConstraints(Experiment experiment) {
        if (experiment.getConstraintsCount() > 0)
            return experiment.getConstraintsList().stream()
                    .map(constraint -> new ConstraintRecord(-1,
                            constraint.getName(),
                            experiment.getId()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }
}
