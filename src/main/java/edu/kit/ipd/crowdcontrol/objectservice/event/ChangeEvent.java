package edu.kit.ipd.crowdcontrol.objectservice.event;

/**
 * Class to abstract a Change event.
 * The object old is the state of the object before the change happend, neww the state after the change.
 * <p>
 * Created by marcel on 02.01.16.
 */
public class ChangeEvent<T> {
    private T old;
    private T neww;

    /**
     * Create a new change event
     *
     * @param old  State before the change
     * @param neww State after the change
     */
    public ChangeEvent(T old, T neww) {
        this.old = old;
        this.neww = neww;
    }

    /**
     * @return old state before the change
     */
    public T getOld() {
        return old;
    }

    /**
     * @return new state after the change
     */
    public T getNeww() {
        return neww;
    }
}
