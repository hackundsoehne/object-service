package edu.ipd.kit.crowdcontrol.objectservice.event;

import rx.Observable;
import rx.Subscriber;

import java.util.LinkedList;

/**
 *
 * Created by marcel on 02.01.16.
 */
public class EventObservable<T> {
    private final Observable<T> observable;
    private final LinkedList<Subscriber<? super T>> subs;

    public EventObservable() {
        subs = new LinkedList<>();
        observable = Observable.create(sub -> {
            synchronized(subs) {
                subs.add(sub);
            }
        });
    }

    /**
     * Will emit the given object as event to the subscribed subscribers of the observable.
     * @param t The object to pass to the events
     */
    public void emit(T t) {
        subs.stream().forEach(subscriber -> subscriber.onNext(t));
    }

    /**
     * Get the observable object of this event
     * @return The observable
     */
    public Observable<T> getObservable() {
        return observable;
    }
}
