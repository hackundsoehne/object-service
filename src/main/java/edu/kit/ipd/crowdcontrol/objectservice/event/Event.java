package edu.kit.ipd.crowdcontrol.objectservice.event;

/**
 * Represents a emitted Event
 * @param <T> Data which is emitted with the event
 * @author MarcelHollerbach
 */
public class Event<T> {
    private T data;
    private EventLog eventLog;

    /**
     * Creates a new event.
     * @param data Data to publish
     */
    public Event(T data) {
        this.data = data;
        this.eventLog = new EventLog();
    }

    /**
     * Get the eventlog for this call.
     * Subscribers can log to this event log.
     *
     * @return a instance of EventLog
     */
    public EventLog getEventLog() {
        return eventLog;
    }

    /**
     * Get the data which is emitted by the event
     * @return
     */
    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Event)) return false;

        Event e = (Event) o;

        return e.data == data;
    }
}
