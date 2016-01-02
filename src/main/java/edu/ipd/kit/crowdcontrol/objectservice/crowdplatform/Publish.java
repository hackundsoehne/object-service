package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 02.01.16.
 */
public interface Publish {
    /**
     * publish a task on the given CrowdPlatform. A worker can now access the Task and do answers.
     * @return true if successful, false if not
     */
    CompletableFuture<Hit> publishTask(Hit hit);

    /**
     * to update a hit simply pass an hit-object with the id of the hit you want to update and
     * the new values.
     * <p>
     * The id has to be the id of the hit you want to update! Updatable are:
     * <ul>
     *      <li>the title
     *      <li>the description
     *      <li>the payment
     * </ul>
     * </p>
     * @param hit the hit to update
     * @return a future which may contains the resulting hit object
     */
    CompletableFuture<Hit> updateTask(Hit hit);

    /**
     * unpublish the task, after this call no answers can be sent for this task
     * @param id the crowd-platform id of the hit
     * @return true if successful, false if not
     */
    CompletableFuture<String> unpublishTask(String id);

}
