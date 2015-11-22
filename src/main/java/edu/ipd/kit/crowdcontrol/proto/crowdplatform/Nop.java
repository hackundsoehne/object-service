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
    public CompletableFuture<Hit> publishTask(Hit hit) {
        System.out.println("pay task: " + hit);
        return CompletableFuture.completedFuture(hit);
    }

    /**
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<Hit> updateTask(Hit hit) {
        System.out.println("updateTask" + hit);
        return CompletableFuture.completedFuture(hit);
    }

    /**
     * unpublish the task, after this call no answers can be sent for this task
     *
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<String> unpublishTask(String id) {
        System.out.println("unpublishTask: " + id);
        return CompletableFuture.completedFuture(id);
    }

    /**
     * Pay Task
     *
     * @param hit
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<Hit> payTask(Hit hit) {
        System.out.println("payTask: " + hit);
        return CompletableFuture.completedFuture(hit);
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
