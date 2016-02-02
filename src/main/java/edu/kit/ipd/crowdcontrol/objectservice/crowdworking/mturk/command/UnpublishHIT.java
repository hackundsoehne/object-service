package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.DisableHITResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.DisableHITResult;

import java.util.HashMap;

/**
 * Created by marcel on 02.02.16.
 */
public class UnpublishHIT extends MturkRestCommand<Boolean, DisableHITResponse> {
    public UnpublishHIT(MTurkConnection connection, String id, String uniqueRestToken) {
        super(connection,uniqueRestToken,
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
