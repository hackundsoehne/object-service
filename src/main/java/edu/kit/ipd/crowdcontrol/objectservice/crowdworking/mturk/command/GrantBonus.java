package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.GrantBonusResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Pay bonus money for a assignment
 * @author MarcelHollerbach
 * @version 1.0
 */
public class GrantBonus extends MturkRestCommand<Boolean, GrantBonusResponse> {

    /**
     * Pay bonus money for a assignment
     * @param connection connection to use
     * @param assignmentId assignment to pay for
     * @param workerId worker to pay
     * @param amount amount of money to pay
     * @param message message why you are paying this
     */
    public GrantBonus(MTurkConnection connection, String assignmentId, String workerId, double amount, String message) {
        super(connection,"GrantBonus", null,"2014-08-15",
                GrantBonusResponse.class,
                () -> {
                    Map<String, Object> values = new HashMap<>();
                    values.put("WorkerId", workerId);
                    values.put("AssignmentId", assignmentId);
                    values.put("BonusAmount.Amount", amount);
                    values.put("BonusAmount.CurrencyCode", "USD");
                    values.put("Reason", message);

                    return values;
                },
                grantBonusResponse -> {
                    Utils.handleRequest(grantBonusResponse.getGrantBonusResult().get(0).getRequest());
                    return true;
                });
    }
}
