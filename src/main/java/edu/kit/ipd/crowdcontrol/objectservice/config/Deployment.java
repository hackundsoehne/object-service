package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * Deployment variables specific to the control ui.
 */
public class Deployment {
    /**
     * Port to listen on
     */
    public Integer port;
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
}
