package edu.ipd.kit.crowdcontrol.objectservice.router;

import rx.Observable;
import rx.Subscriber;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.LinkedList;
import java.util.function.BiConsumer;

/**
 * Created by marcel on 17.12.15.
 */
public class ResponseEventHandler {
    private Subscriber<? super ResponseEvent> resp;
    private Observable<ResponseEvent> observable;

    public ResponseEventHandler(BiConsumer<String, Route> provider, String path) {
        resp = null;
        observable = Observable.create(subscriber -> {
            if (resp != null) throw new IllegalStateException("This Request Event is already subscribed");
            resp = subscriber;
        });
        provider.accept(path, this::handle);
    }
    private Object handle(Request request, Response response){
        ResponseEvent responseEvent = new ResponseEvent(null, request);
        resp.onNext(responseEvent);

        return responseEvent.getResponse();
    }
}
