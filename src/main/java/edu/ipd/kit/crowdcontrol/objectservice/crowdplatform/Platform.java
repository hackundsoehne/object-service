package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import java.util.Optional;

/**
 * This interface just presents getters to other interfaces.
 *
 * With returning a none null optional the implementor agrees to handle the job offered by the interface to do by itself
 * If null is returned the own services will do the work of this interface
 *
 * Created by marcel on 02.01.16.
 */
public interface Platform {
    /**
     * Return the Payment interface to work for this platform
     * @return
     */
    Optional<Payment> getPayment();

    /**
     * Will return the Assertion interface to work for this platform.
     * @return
     */
    Optional<Assertion> getAssertion();

    /**
     * return the interface which can publish hits
     * This MUST return non null
     * @return A instance which can handling publishing of hits
     */
    Publish getPublish();

    /**
     * Get the name of this platform
     * @return A unique String
     */
    String getName();
}
