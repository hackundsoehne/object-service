package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;

/**
 * Created by marcel on 26.01.16.
 */
public class TagTransformer {
    public static Tag toProto(TagRecord record) {
        return Tag.newBuilder()
                .setName(record.getTag()).build();
    }
}
