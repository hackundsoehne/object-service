package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import edu.ipd.kit.crowdcontrol.proto.controller.InternalServerErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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
     * @return The optional crowd platform instance
     */
    public Optional<CrowdPlatform> getCrowdPlatform(String name) {
        return Optional.ofNullable(platforms.get(name));
    }

    public CompletableFuture<Hit> publishHit(Hit hit, BiConsumer<Hit, Throwable> callback) {
        return getCrowdPlatform(hit.getPlatform())
                .map(platform -> platform.publishTask(hit))
                .orElseThrow(() -> new InternalServerErrorException("platform for hit " + hit + " is not available"))
                .handle((hitR, ex) -> {
                    callback.accept(hit, ex);
                    return hit;
                });
    }

    public CompletableFuture<Hit> updateHit(Hit hit) {
        return getCrowdPlatform(hit.getPlatform())
                .map(platform -> platform.updateTask(hit))
                .orElseThrow(() -> new InternalServerErrorException("platform for hit " + hit + " is not available"));
    }
}
