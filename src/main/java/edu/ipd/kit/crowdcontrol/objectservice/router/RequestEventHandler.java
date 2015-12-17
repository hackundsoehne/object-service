package edu.ipd.kit.crowdcontrol.objectservice.router;

import org.jooq.lambda.tuple.Tuple2;
import rx.Observable;
import rx.Subscriber;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by marcel on 16.12.15.
 */
public class RequestEventHandler {
    private Observable<RequestEvent> observable;
    private List<Subscriber<? super RequestEvent>> subs;

    public RequestEventHandler(BiConsumer<String, Route> provider, String path) {
        subs = new LinkedList();
        observable = Observable.create(subscriber -> subs.add(subscriber));
        provider.accept(path, this::handle);
    }

    private RequestEvent emit(Subscriber<? super RequestEvent> sub, Request req) {
        RequestEvent ev = new RequestEvent(req);
        sub.onNext(ev);
        return ev;
    }
    private Object handle(Request request, Response response){
        if (subs.stream().map(subscriber -> emit(subscriber, request))
                .allMatch(errorObject -> !errorObject.isFailed()))
            return "success";
        return "fail";
    }

    public Observable<RequestEvent> getObservable() {
        return observable;
    }
}
