package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

/**
 * Created by marcel on 24.01.16.
 */
public class UnknownWorkerException extends Exception {
    public UnknownWorkerException() {
        super("Worker could not be found");
    }
}
