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
public class RequestEvent {
    private Observable<Tuple2<Request, ErrorObject>> observable;
    private List<Subscriber<? super Tuple2<Request, ErrorObject>>> subs;

    public RequestEvent(BiConsumer<String, Route> provider, String path) {
        subs = new LinkedList();
        observable = Observable.create(subscriber -> subs.add(subscriber));
        provider.accept(path, this::handle);
    }

    private ErrorObject emit(Subscriber<? super Tuple2<Request, ErrorObject>> sub, Request req) {
        ErrorObject errObject = new ErrorObject();
        sub.onNext(new Tuple2<>(req, errObject));
        return errObject;
    }
    private Object handle(Request request, Response response){
        if (subs.stream().map(subscriber -> emit(subscriber, request))
                .allMatch(errorObject -> !errorObject.isFailed()))
            return "success";
        return "fail";
    }

    public Observable<Tuple2<Request, ErrorObject>> getObservable() {
        return observable;
    }
}
