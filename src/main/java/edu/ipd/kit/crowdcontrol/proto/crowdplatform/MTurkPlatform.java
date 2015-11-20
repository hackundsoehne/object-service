package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import com.amazonaws.mturk.addon.BatchItemCallback;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.ClientConfig;

import java.util.function.Consumer;

/**
 * Created by marcel on 20.11.15.
 */
public class MTurkPlatform implements CrowdPlatform {
    private final String password;
    private final String username;
    private final String url;
    private ClientConfig config;
    private RequesterService service;
    public MTurkPlatform(String username, String password, String url) {
        this.password = password;
        this.username = username;
        this.url = url;

        config = new ClientConfig();
        config.setAccessKeyId(this.username);
        config.setSecretAccessKey(this.password);
        config.setServiceURL(this.url);

        service = new RequesterService(config);
    }

    @Override
    public boolean publishTask(Hit hit) {
        String keywords = "";
        for(String keyword : hit.getTags()) {
            keywords += keyword;
        }

        //30 days in seconds
        long assignment = 30*24*60*60;

        String question = "<?xml version=\"1.0\"?>\n<ExternalQuestion " +
                "xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd\">" +
                "<ExternalURL>"+hit.getUrl()+"</ExternalURL>" +
                "<FrameHeight>200</FrameHeight>" +
                "</ExternalQuestion>";

        HIT mhit = service.createHIT(hit.getTitle(), hit.getTitle(), hit.getDescription(), keywords, question, hit.getPayment(), hit.getAssignmentDuration(), assignment, hit.getHitDuration(), hit.getAmount(), null, null, null);

        if (mhit == NULL)
            return false;
        return true;
    }

    @Override
    public boolean updateTask(Hit hit) {
        int assignmentIncrement = (int) (hit.getAssignmentDuration() - mhit.getAssignmentDurationInSeconds());

        if (increment < 0) {
            System.err.println("Assignment duration has to be bigger");
            return false;
        }

        String keywords = "";
        for(String keyword : hit.getTags()) {
            keywords += keyword;
        }

        service.extendHIT(hit.getId(), assignmentIncrement, 0);
        service.updateHIT(hit.getId(), hit.getTitle(), hit.getDescription(), keywords, hit.getPayment());

        return true;
    }

    @Override
    public boolean unpublishTask(Hit hit) {
        HIT mhit = service.getHIT(hit.getId());

        if (mhit == null) return false;

        String[] hitids = {hit.getId()};

        service.deleteHITs(hitids, false, true, new BatchItemCallback() {
            @Override
            public void processItemResult(Object o, boolean b, Object o1, Exception e) {

            }
        });
    }

    @Override
    public boolean payTask(Hit hit) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }
}
