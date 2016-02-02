package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.GetHITResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.HIT;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by marcel on 02.02.16.
 */
public class GetHIT extends MturkRestCommand<HIT, GetHITResponse> {
    /**
     * Get a HIT from a given platform
     * @param con
     * @param id
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
