package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Platform;

/**
 * Transforms platform protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class PlatformTransform {
    /**
     * Converts a platform record to its protobuf representation.
     *
     * @param record platform record
     *
     * @return Platform.
     */
    public static Platform toProto(PlatformRecord record) {
        return Platform.newBuilder()
                .setId(record.getIdPlatform())
                .setName(record.getName())
                .build();
    }
}
