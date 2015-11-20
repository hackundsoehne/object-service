package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by marcel on 20.11.15.
 */
public class CrowdPlatformManager {
    private final Map<String, CrowdPlatform> platforms;

    public CrowdPlatformManager(List<CrowdPlatform> crowdPlatforms) {
        platforms = crowdPlatforms.stream()
                .collect(Collectors.toMap(CrowdPlatform::getName, Function.identity()));
    }

    /**
     * Will get you the instance of a crowd platform, this instance is the same for all calls
     * @param name The name of the instance to use
     * @return The crowd platform instance
     * @throws CrowdPlatformNotFoundException If the name is unknown this exception will be thrown
     */
    public CrowdPlatform getCrowdplatform(String name) throws CrowdPlatformNotFoundException {
        CrowdPlatform searched;

        //first check the cache
        searched = platforms.get(name);

        if (searched != null)
            return searched;
        //fail if it is still not found
        throw new CrowdPlatformNotFoundException(name+"Could not be found");
    }
}
