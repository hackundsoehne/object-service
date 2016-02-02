package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.mturk.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Class to deal with rest api provided by mturk
 * @author MarcelHollerbach
 * @version 0.1
 */
public class MTurkRestApi {
    private final MTurkConnection connection;

    public MTurkRestApi(String awsAccessKeyId, String awsSecretAccessKey, String url) {
        this.connection = new MTurkConnection(awsAccessKeyId,awsSecretAccessKey,url);
    }

    /**
     * Tries to publish a hit with the given parameters
     * @param description The description of the HIT
     * @param title The title which is displayed representing for the hit
     * @param reward money a worker gets when he finished the hit
     * @param assignmentDurationInSeconds duration of the assignment before it times out
     * @param lifetimeInSeconds how long a hit is published before it is auto closed
     * @param keywords comma separated list of keywords which are used to find this hit
     * @param maxAssignments maximum number of assignments before this hit is closed
     * @param autoApprovalDelayInSeconds after how many seconds a assignment is approved
     * @param data is attached to the hit and can be accessed later
     * @param uniqueRequestToken The unique request token of the call
     */
    public CompletableFuture<String> publishHIT(String description,
                                        String title, double reward, int assignmentDurationInSeconds,
                                        int lifetimeInSeconds, String keywords, int maxAssignments, int autoApprovalDelayInSeconds,
                                        String data, String uniqueRequestToken) {
        Map<String, Object> values = new HashMap<>();

        values.put("Title", title);
        values.put("Description", description);
        values.put("Question",
                        "<ExternalQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd\">\n" +
                                "  <ExternalURL>https://tictactoe.amazon.com/gamesurvey.cgi?gameid=01523</ExternalURL>\n" +
                                "  <FrameHeight>400</FrameHeight>\n" +
                                "</ExternalQuestion>");//FIXME external reference
        values.put("Reward.Amount", reward);
        values.put("Reward.CurrencyCode", "USD"); //FIXME
        values.put("AssignmentDurationInSeconds", assignmentDurationInSeconds);
        values.put("LifetimeInSeconds", lifetimeInSeconds);
        values.put("Keywords", keywords);
        values.put("MaxAssignments", maxAssignments);
        values.put("AutoApprovalDelayInSeconds", autoApprovalDelayInSeconds);
        //FIXME assignmentReviewPolicy?
        //FIXME HITReviewPolicy
        values.put("RequesterAnnotation", data);

        return new MturkRestCommand<>(connection,uniqueRequestToken,
                "CreateHIT","Minimal","2014-08-15",
                CreateHITResponse.class,values,createHITResponse -> {
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

/*        return new MturkRestCommand<>(url, "CreateHIT", values, staticDefaultValues,
                CreateHITResponse.class, awsSecretAccessKey, "Minimal", "2014-08-15",
                uniqueRequestToken,*/
    }

    /**
     * Unpublish a before published HIT
     * @param id hit id which is used to find the HIT
     * @return True on success otherwise a exception
     */
    public CompletableFuture<Boolean> unpublishHIT(String id,String uniqueRestToken) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("HITId",id);
        return new MturkRestCommand<>(connection, uniqueRestToken,
                "DisableHIT","Request","2014-08-15",DisableHITResponse.class,
                values,disableHITResult -> {

                    //throw exception if there was a error
                    Utils.handleRequest(disableHITResult.getDisableHITResult().get(0).getRequest());

                    //if not everything is fine
                    return true;
                });

        /*return new MturkRestCommand<>(url, "DisableHIT", values, staticDefaultValues,
                DisableHITResponse.class, awsSecretAccessKey,
                "Request","2014-08-15",*/
    }

    /**
     * Get a specific HIT
     * @param id hit id which was returned by CreateHIT
     * @return the datastructure of a exception
     */
    public CompletableFuture<HIT> getHit(String id, String uniqueRestToken) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("HITId", id);
        return new MturkRestCommand<>(connection,uniqueRestToken,
                "GetHIT","HITDetail","2014-08-15", GetHITResponse.class,
                values,getHITResponse -> {
                    if (getHITResponse.getHIT().size() < 1)
                        throw new IllegalArgumentException("Hit "+id+" not found!");
                    Utils.handleRequest(getHITResponse.getHIT().get(0).getRequest());
                    return getHITResponse.getHIT().get(0);
                });
        /*return new MturkRestCommand<>(url, "GetHIT", values, staticDefaultValues,
                GetHITResponse.class, awsSecretAccessKey, "HITDetail","2014-08-15",getHITResponse -> {
            if (getHITResponse.getHIT().size() < 1)
                throw new IllegalArgumentException("Hit "+id+" not found!");
            Utils.handleRequest(getHITResponse.getHIT().get(0).getRequest());
            return getHITResponse.getHIT().get(0);
        });*/
    }
}
