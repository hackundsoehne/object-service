package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.Worker;

import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 02.01.16.
 */
public interface Payment {
    /**
     * Pay a worker for his work
     * @param w The worker to pay
     * @param ammount The amout of money, (given in the smallest parts available in a currency example: cents)
     * @return if everything went fine or not
     */
    CompletableFuture<Boolean> payTask(Worker w, int ammount);
}
