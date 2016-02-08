package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.ApproveAssignmentResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Approve a assignment
 * @author MarcelHollerbach
 * @version 1.0
 */
public class ApproveAssignment extends MturkRestCommand<Boolean, ApproveAssignmentResponse> {
    /**
     * Approve a Assignment
     * @param connection connection to use
     * @param assignmentId assignment id to approve
     * @param message message to pass to the worker
     */
    public ApproveAssignment(MTurkConnection connection, String assignmentId, String message) {
        super(connection,"ApproveAssignment",null,"2014-08-15",
                ApproveAssignmentResponse.class,
                () -> {
                    Map<String, Object> values = new HashMap<String, Object>();
                    values.put("AssignmentId", assignmentId);
                    values.put("RequesterFeedback", message);
                    return values;
                },
                approveAssignmentResponse -> {
                    Utils.handleRequest(
                            approveAssignmentResponse.getApproveAssignmentResult().get(0).getRequest()
                    );
                    return true;
                });
    }
}
