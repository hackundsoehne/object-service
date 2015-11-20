package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * Created by marcel on 20.11.15.
 */
public interface CrowdPlatform {
    /**
     * publish a task on the given CrowdPlatform. A worker can now access the Task and do answers
     */
    void publishTask(String id, String name, String url, int amount);

    /**
     *
     */
    void updateTask(String id, String name, String url, int amount);

    /**
     * unpublish the task, after this call no answers can be sent for this task
     */
    void unpublishTask(String id);

    /**
     * Pay Task
     */
    void payTask(String id, int payment);

    String getName();
}