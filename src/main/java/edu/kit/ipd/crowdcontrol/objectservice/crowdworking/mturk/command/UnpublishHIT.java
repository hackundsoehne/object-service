package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.DisableHITResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;

import java.util.HashMap;

/**
 * Remove a HIT from mturk
 * @version 1.0
 * @author MarcelHollerbach
 */
public class UnpublishHIT extends MturkRestCommand<Boolean, DisableHITResponse> {
    public UnpublishHIT(MTurkConnection connection, String id) {
        super(connection,
                "DisableHIT","Request","2014-08-15",DisableHITResponse.class,
                () -> {
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("HITId",id);
                    return values;
                },
                disableHITResponse -> {
                    //throw exception if there was a error
                    Utils.handleRequest(disableHITResponse.getDisableHITResult().get(0).getRequest());

                    //if not everything is fine
                    return true;
                }
        );
    }
}
