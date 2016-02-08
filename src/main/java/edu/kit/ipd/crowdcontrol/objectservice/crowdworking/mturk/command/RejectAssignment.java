package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.RejectAssignmentResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Reject a Assignment
 * @author MarcelHollerbach
 * @version 1.0
 */
public class RejectAssignment extends MturkRestCommand<Boolean, RejectAssignmentResponse> {
    /**
     * Reject a assignment
     * @param connection connection to use
     * @param assignmentId assignment to reject
     * @param message a short string why the assignment was rejected
     */
    public RejectAssignment(MTurkConnection connection, String assignmentId, String message) {
        super(connection,"RejectAssignment",null,"2014-08-15",
                RejectAssignmentResponse.class,
                () -> {
                    Map<String, Object> values = new HashMap<String, Object>();
                    values.put("AssignmentId", assignmentId);
                    values.put("RequesterFeedback", message);
                    return values;
                },
                rejectAssignmentResponse -> {
                    Utils.handleRequest(
                            rejectAssignmentResponse.getRejectAssignmentResult().get(0).getRequest()
                    );
                            return true;
                });
    }
}
