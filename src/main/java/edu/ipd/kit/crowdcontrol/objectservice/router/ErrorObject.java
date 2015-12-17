package edu.ipd.kit.crowdcontrol.objectservice.router;

/**
 * Created by marcel on 17.12.15.
 */
public class ErrorObject {
    private boolean failed;
    private String message;

    public ErrorObject() {
        this.failed = false;
        this.message = "";
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getMessage() {
        return message;
    }
}
