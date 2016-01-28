package edu.kit.ipd.crowdcontrol.objectservice.event;

import rx.Observable;
import rx.Subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by marcel on 02.01.16.
 */
public class EventObservable<T> {
    private final Observable<Event<T>> observable;
    private final LinkedList<Subscriber<? super Event<T>>> subs;

    public EventObservable() {
        subs = new LinkedList<>();
        observable = Observable.create(sub -> {
            synchronized (subs) {
                subs.add(sub);
            }
        });
    }

    /**
     * Will emit the given object as event to the subscribed subscribers of the observable.
     *
     * @param t The object to pass to the events
     * @return The messages returned by the emits
     */
    public List<EventLog> emit(T t) {
        return subs.stream()
                .map(subscriber -> eventEmit(subscriber, t))
                .collect(Collectors.toList());
    }

    private EventLog eventEmit(Subscriber<? super Event<T>> subscriber, T t) {
        Event<T> event = new Event<>(t);
        subscriber.onNext(event);
        return event.getEventLog();
    }

    /**
     * Get the observable object of this event
     *
     * @return The observable
     */
    public Observable<Event<T>> getObservable() {
        return observable;
    }
}
