package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * Deployment variables specific to the control ui.
 */
public class Deployment {
    /**
     * Port to listen on
     */
    public int port = 4567;
    /**
     * Deployment origin of the control-ui
     */
    public String origin;
    /**
     * The url of the worker-service
     */
    public String workerService;
    /**
     * The local url of the worker-ui
     */
    public String workerUILocal;
    /**
     * The public url of the worker-ui
     */
    public String workerUIPublic;
    /**
     * The time to wait before a experiment is set to finished. Given in minutes.
     * Really only set this value if you know what you are doing.
     * This should only be used for debugging purpose and never in a productive system
     */
    public int taskWaitBeforeFinish = 120;

    /**
     * the jwtsecret for the jwt-token.
     */
    public String jwtsecret;
}
