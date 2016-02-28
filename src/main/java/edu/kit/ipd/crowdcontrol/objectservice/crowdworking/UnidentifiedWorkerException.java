package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

/**
 * Created by marcel on 24.01.16.
 */
public class UnidentifiedWorkerException extends RuntimeException {
    public UnidentifiedWorkerException(String message) {
        super(message);
    }

    public UnidentifiedWorkerException() {
        super("Illegal Request");
    }
}
