package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import java.util.Map;
import java.util.Optional;

/**
 * Created by marcel on 19.01.16.
 */
public interface Worker {
    /**
     * Parse a worker id out of the params
     * @param param The parameters which were sent by a platform
     * @return The id of the worker if one can be found
     */
    Optional<String> identifyWorker(Map<String, String[]> param);
}
