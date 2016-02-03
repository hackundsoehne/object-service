package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.CreateHITResponse;
import com.amazonaws.mturk.requester.doc._2014_08_15.HIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Create a HIT
 * @version 1.0
 * @author MarcelHollerbach
 */
public class PublishHIT extends MturkRestCommand<String, CreateHITResponse> {
    /**
     * Tries to publish a hit with the given parameters
     * @param connection connection to use for creating the HIT
     * @param title The title which is displayed representing for the hit
     * @param description The description of the HIT
     * @param reward money a worker gets when he finished the hit
     * @param assignmentDurationInSeconds duration of the assignment before it times out
     * @param lifetimeInSeconds how long a hit is published before it is auto closed
     * @param keywords comma separated list of keywords which are used to find this hit
     * @param maxAssignments maximum number of assignments before this hit is closed
     * @param autoApprovalDelayInSeconds after how many seconds a assignment is approved
     * @param data is attached to the hit and can be accessed later
     */
    public PublishHIT(MTurkConnection connection, Object title,
                      String description, double reward, int assignmentDurationInSeconds,
                      int lifetimeInSeconds, String keywords, int maxAssignments,
                      int autoApprovalDelayInSeconds, String data) {
        super(connection,
                "CreateHIT","Minimal","2014-08-15",CreateHITResponse.class,
                () -> {
                    Map<String, Object> values = new HashMap<>();

                    values.put("Title", title);
                    values.put("Description", description);
                    values.put("Question",
                            "<ExternalQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd\">\n" +
                                    "  <ExternalURL>https://tictactoe.amazon.com/gamesurvey.cgi?gameid=01523</ExternalURL>\n" +
                                    "  <FrameHeight>400</FrameHeight>\n" +
                                    "</ExternalQuestion>");//FIXME external reference
                    values.put("Reward.Amount", reward);
                    values.put("Reward.CurrencyCode", "USD");
                    values.put("AssignmentDurationInSeconds", assignmentDurationInSeconds);
                    values.put("LifetimeInSeconds", lifetimeInSeconds);
                    values.put("Keywords", keywords);
                    values.put("MaxAssignments", maxAssignments);
                    values.put("AutoApprovalDelayInSeconds", autoApprovalDelayInSeconds);
                    values.put("RequesterAnnotation", data);
                    return values;
                },
                createHITResponse -> {
                    // check if we got a hit at all
                    if (createHITResponse.getHIT().size() == 0) {
                        throw new IllegalStateException("No result HIT!");
                    }

                    //get the created HIT
                    HIT created = createHITResponse.getHIT().get(0);

                    //check for errors
                    Utils.handleRequest(created.getRequest());

                    //return HITId
                    return created.getHITId();
                });
    }
}
