package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 19.01.16.
 */
public interface Payment {
    /**
     * Called to pay a worker the given amount of Money
     *
     * @param worker The given worker to pay
     * @param amount The amount to pay, this should be calculated as cents of dollars
     * @return True if everything went fine, false if not
     */
    CompletableFuture<Boolean> payWorker(Worker worker, int amount);
}
