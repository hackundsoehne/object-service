package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.NotifyWorkersResponse;
import com.amazonaws.mturk.requester.doc._2014_08_15.NotifyWorkersResult;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by marcel on 10.03.16.
 */
public class NotifyWorker extends MturkRestCommand<Boolean, NotifyWorkersResponse> {
    public static final int MAX_LENGTH = 4096;
    /**
     * Creates a new command
     *
     * @param con connection to use
     * @param workerId worker to sent the message to
     * @param subject the subject for the message
     * @param message message for the user
     */
    public NotifyWorker(MTurkConnection con, String workerId, String subject, String message) {
        super(con, "NotifyWorkers", null, "2014-08-15",
                NotifyWorkersResponse.class,
                () -> {
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("Subject", subject);
                    values.put("MessageText", message);
                    values.put("WorkerId", workerId);
                    return values;
                },
                notifyWorkersResult -> {
                    Utils.handleRequest(notifyWorkersResult.getNotifyWorkersResult().get(0).getRequest());
                    return true;
                });
    }
}
