package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.BonusPayment;
import com.amazonaws.mturk.requester.doc._2014_08_15.GetBonusPaymentsResponse;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marcel on 04.02.16.
 */
public class GetBonusPayments extends MturkRestCommand<List<BonusPayment>, GetBonusPaymentsResponse> {

    public GetBonusPayments(MTurkConnection connection, String hitId, String assignmentId, int pagenumber) {
        super(connection,
                "GetBonusPayments",null,"2014-08-15",GetBonusPaymentsResponse.class,
                () -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("HITId", hitId);
                    map.put("AssignmentId", assignmentId);
                    map.put("PageSize", "100");
                    map.put("PageNumber", pagenumber);

                    return map;
                },
                getBonusPaymentsResponse -> {
                    Utils.handleRequest(getBonusPaymentsResponse.getGetBonusPaymentsResult().get(0).getRequest());

                    return getBonusPaymentsResponse.getGetBonusPaymentsResult().get(0).getBonusPayment();
                });
    }
}
