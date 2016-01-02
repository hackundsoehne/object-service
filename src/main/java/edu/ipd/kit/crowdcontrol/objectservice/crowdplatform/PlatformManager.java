package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by marcel on 02.01.16.
 */
public class PlatformManager {
    private final Map<String, Platform> platforms;
    private final Assertion dbAssertion;
    private final Payment servicPayment;

    public PlatformManager(List<Platform> crowdPlatforms) {
        //TODO the two interfaces can be passed in the contstructor to allow external fallback handing
        dbAssertion = new DataBaseAssertion();
        servicPayment = new ServicePayment();
        platforms = crowdPlatforms.stream()
                .collect(Collectors.toMap(Platform::getName, Function.identity()));
    }

    /**
     * Will get you the instance of a platform interface of a platform, this instance is the same for all calls
     * @param name The name of the instance to use
     * @return The optional crowd platform instance
     */
    public Optional<Platform> getPlatform(String name) {
        return Optional.ofNullable(platforms.get(name));
    }
    /**
     * Will get you the instance of a publish interface of a platform, this instance is the same for all calls
     * @param name The name of the instance to use
     * @return The optional crowd platform instance
     */
    public Optional<Publish> getPlatformPublish(String name) {
        return getPlatform(name).map(platform -> platform.getPublish());
    }

    /**
     * Will get you the instance of a assertion interface of a platform, this instance is the same for all calls
     * @param name The name of the instance to use
     * @return The optional crowd platform instance
     */
    public Optional<Assertion> getPlatformAssertion(String name) {
        return getPlatform(name).map(platform -> platform.getAssertion().orElse(dbAssertion));
    }

    /**
     * Will get you the instance of a payment interface of a platform, this instance is the same for all calls
     * @param name The name of the instance to use
     * @return The optional crowd platform instance
     */
    public Optional<Payment> getPlatformPayment(String name) {
        return getPlatform(name).map(platform -> platform.getPayment().orElse(servicPayment));
    }
}
