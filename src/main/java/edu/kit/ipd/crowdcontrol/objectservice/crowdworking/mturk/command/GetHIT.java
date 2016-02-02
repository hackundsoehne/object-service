package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.GetHITResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.HIT;

import java.util.HashMap;

/**
 * Get a HIT from mturk
 * @version 1.0
 * @author MarcelHollerbach
 */
public class GetHIT extends MturkRestCommand<HIT, GetHITResponse> {
    /**
     * Get a HIT
     * @param con connection to use
     * @param id id of the HIT to get
     */
    public GetHIT(MTurkConnection con, String id){
        super(con,
                "GetHIT","HITDetail","2014-08-15", GetHITResponse.class,
                () -> {
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("HITId", id);
                    return values;
                },
                getHITResponse -> {
                    if (getHITResponse.getHIT().size() < 1)
                        throw new IllegalArgumentException("Hit "+id+" not found!");
                    Utils.handleRequest(getHITResponse.getHIT().get(0).getRequest());
                    return getHITResponse.getHIT().get(0);
                });
    }
}
