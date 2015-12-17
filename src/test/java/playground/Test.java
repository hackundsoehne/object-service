package playground;

import edu.ipd.kit.crowdcontrol.objectservice.router.RequestEventHandler;
import edu.ipd.kit.crowdcontrol.objectservice.router.ResponseEventHandler;
import spark.Spark;

import static spark.Spark.port;

/**
 * Created by marcel on 17.12.15.
 */
public class Test {
    public static void main(String[] args) {
        port(4567);

        RequestEventHandler sucesshandler = new RequestEventHandler(Spark::get, "/suc");
        sucesshandler.getObservable().subscribe(req -> System.out.println("bla1"+req.getReq().ip()));

        RequestEventHandler failedhandler = new RequestEventHandler(Spark::get, "/fail");
        failedhandler.getObservable().subscribe(req ->
        {
            req.setFailed(true);
            req.setFailedmessage("test fail");
        });

        ResponseEventHandler handler = new ResponseEventHandler(Spark::get, "/resp");
        handler.getObservable().subscribe(resp -> resp.setResponse("Test"));

    }
}
