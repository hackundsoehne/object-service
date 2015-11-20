package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * Created by marcel on 20.11.15.
 */
public interface CrowdPlatform {
    /**
     * publish a task on the given CrowdPlatform. A worker can now access the Task and do answers.
     * @return true if successful, false if not
     */
    boolean publishTask(String id, String name, String url, int amount);

    /**
     * @return true if successful, false if not
     */
    boolean updateTask(String id, String name, String url, int amount);

    /**
     * unpublish the task, after this call no answers can be sent for this task
     * @return true if successful, false if not
     */
    boolean unpublishTask(String id);

    /**
     * Pay Task
     * @return true if successful, false if not
     */
    boolean payTask(String id, int payment);

    /**
     * the name of the CrowdPlatform
     * @return true if successful, false if not
     */
    String getName();
}