package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles transformations of tags and constraints from and to the protocol buffer messages.
 *
 * @author Marcel Hollerbach
 */
public class TagConstraintTransformer extends AbstractTransformer {
    /**
     * Converts a record into a proto object
     * @param record The record to convert
     * @return A proto object with the data from the record
     */
    public static Tag toTagProto(TagRecord record) {
        return Tag.newBuilder()
                .setName(record.getTag()).build();
    }

    /**
     * Creates a TagRecord list form a given experiment
     * @param experiment the experiment to extract data from
     * @param experimentId the experimentId
     * @return A list of tags used by the experiment
     */
    public static List<TagRecord> getTags(Experiment experiment, int experimentId) {
        if (experiment.getTagsCount() > 0)
            return experiment.getTagsList().stream()
                    .map(tag ->
                    new TagRecord(null,
                            tag.getName(),
                            experimentId))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    /**
     * Converts a record into the protobuf object
     * @param record The record to take data from
     * @return A new Constraint object
     */
    public static Constraint toConstraintsProto(ConstraintRecord record) {
        return Constraint.newBuilder()
                .setName(record.getConstraint()).build();
    }

    /**
     * Create a list of Constraints from a given experiment
     * @param experiment The experiment to grab data from
     *
     * @return A list of constraintRecords
     */
    public static List<ConstraintRecord> getConstraints(Experiment experiment, int experimentId) {
        if (experiment.getConstraintsCount() > 0)
            return experiment.getConstraintsList().stream()
                    .map(constraint -> new ConstraintRecord(null,
                            constraint.getName(),
                            experimentId))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }
}
