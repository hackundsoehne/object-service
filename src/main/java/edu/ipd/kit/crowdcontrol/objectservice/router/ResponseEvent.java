package edu.ipd.kit.crowdcontrol.objectservice.router;

import spark.Request;

/**
 * Created by marcel on 17.12.15.
 */
public class ResponseEvent {
    private Object response;
    private Request request;
    private boolean failed;

    public ResponseEvent(Object response, Request req) {
        this.response = response;
        this.request = req;
    }

    public Object getResponse() {
        return response;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public Request getRequest() {
        return request;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
