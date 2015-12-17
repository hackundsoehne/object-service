package edu.ipd.kit.crowdcontrol.objectservice.router;

import spark.Request;

/**
 * Created by marcel on 17.12.15.
 */
public class RequestEvent {
    private Request req;
    private boolean failed;
    private String failedmessage;

    public RequestEvent(Request req) {
        this.req = req;
        this.failed = false;
        this.failedmessage = "";
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public void setFailedmessage(String failedmessage) {
        this.failedmessage = failedmessage;
    }

    public Request getReq() {
        return req;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getFailedmessage() {
        return failedmessage;
    }
}
