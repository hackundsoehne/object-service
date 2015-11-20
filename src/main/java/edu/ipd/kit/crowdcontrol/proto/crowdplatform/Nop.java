package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 20.11.15.
 */
public class Nop implements CrowdPlatform {

    /**
     * publish a task on the given CrowdPlatform. A worker can now access the Task and do answers.
     *
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<Boolean> publishTask(Hit hit) {
        System.out.println("pay task: " + hit);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<Boolean> updateTask(Hit hit) {
        System.out.println("updateTask" + hit);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * unpublish the task, after this call no answers can be sent for this task
     *
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<Boolean> unpublishTask(Hit hit) {
        System.out.println("unpublishTask: " + hit);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Pay Task
     *
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<Boolean> payTask(Hit hit) {
        System.out.println("payTask: " + hit);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * the name of the CrowdPlatform
     *
     * @return true if successful, false if not
     */
    @Override
    public String getName() {
        return "NOP";
    }
}
