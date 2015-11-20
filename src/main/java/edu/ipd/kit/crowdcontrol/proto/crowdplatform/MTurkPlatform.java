package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import com.amazonaws.mturk.addon.BatchItemCallback;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.ClientConfig;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    public CompletableFuture<Boolean> publishTask(Hit hit) {
        String keywords = hit.getTags().stream()
                .collect(Collectors.joining(","));

        //30 days in seconds
        long assignment = 30*24*60*60;

        String question = "<?xml version=\"1.0\"?>\n<ExternalQuestion " +
                "xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd\">" +
                "<ExternalURL>"+hit.getUrl()+"</ExternalURL>" +
                "<FrameHeight>200</FrameHeight>" +
                "</ExternalQuestion>";

        return CompletableFuture.supplyAsync(() -> {
            HIT hit1 = service.createHIT(hit.getTitle(), hit.getTitle(), hit.getDescription(), keywords, question, hit.getPayment(), hit.getAssignmentDuration(), assignment, hit.getHitDuration(), hit.getAmount(), null, null, null);
            return hit1 != null;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateTask(Hit hit) {
        int assignmentIncrement = (int) (hit.getAssignmentDuration() - mhit.getAssignmentDurationInSeconds());

        if (increment < 0) {
            System.err.println("Assignment duration has to be bigger");
            return CompletableFuture.completedFuture(false);
        }

        String keywords = hit.getTags().stream()
                .collect(Collectors.joining(","));

        return CompletableFuture.supplyAsync(() -> {
            service.extendHIT(hit.getId(), assignmentIncrement, 0);
            //FIXME newhit
            service.updateHIT(hit.getId(), hit.getTitle(), hit.getDescription(), keywords, hit.getPayment());
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(Hit hit) {
        HIT mhit = service.getHIT(hit.getId());

        if (mhit == null) return CompletableFuture.completedFuture(false);

        String[] hitids = {hit.getId()};

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        service.deleteHITs(hitids, false, true, (o, b, o1, e) -> result.complete(b));
        return result;
    }

    @Override
    public CompletableFuture<Boolean> payTask(Hit hit) {
        //
        return false;
    }

    @Override
    public String getName() {
        return null;
    }
}
