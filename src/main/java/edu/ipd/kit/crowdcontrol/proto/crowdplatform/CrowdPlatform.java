package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 20.11.15.
 */
public interface CrowdPlatform {

    /**
     * publish a task on the given CrowdPlatform. A worker can now access the Task and do answers.
     * @return true if successful, false if not
     */
    CompletableFuture<Hit> publishTask(Hit hit);

    /**
     * @return true if successful, false if not
     */
    CompletableFuture<Hit> updateTask(Hit hit);

    /**
     * unpublish the task, after this call no answers can be sent for this task
     * @return true if successful, false if not
     */
    CompletableFuture<Hit> unpublishTask(Hit hit);

    /**
     * unpublish the task, after this call no answers can be sent for this task
     * @return true if successful, false if not
     */
    CompletableFuture<String> unpublishTask(String id);

    /**
     * Pay Task
     * @return true if successful, false if not
     */
    CompletableFuture<Hit> payTask(Hit hit);

    /**
     * the name of the CrowdPlatform
     * @return true if successful, false if not
     */
    String getName();
}