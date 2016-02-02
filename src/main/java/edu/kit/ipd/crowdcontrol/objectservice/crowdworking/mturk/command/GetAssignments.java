package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.Assignment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.GetAssignmentsForHITResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Get all assignments of a hit
 * @author MarcelHollerbach
 * @version 1.0
 */
public class GetAssignments extends MturkRestCommand<List<Assignment>, GetAssignmentsForHITResponse> {
    /**
     * Get assignments from a specific hit
     * @param connection connection to use
     * @param id hit id to get the assignments for
     * @param pagenumber this is paged by a size of 100 assignments per page
     */
    public GetAssignments(MTurkConnection connection, String id, int pagenumber) {
        super(connection,"GetAssignmentsForHIT", "", "2014-08-15", GetAssignmentsForHITResponse.class,
                () -> {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("HITId", id);
                    hashMap.put("PageSize", 100);
                    hashMap.put("PageNumber", pagenumber);
                    return  hashMap;
                },
                getAssignmentsForHITResponse -> {
                    Utils.handleRequest(getAssignmentsForHITResponse.getGetAssignmentsForHITResult().get(0)
                            .getRequest());

                    return getAssignmentsForHITResponse.getGetAssignmentsForHITResult().get(0).getAssignment();
                });
    }
}
