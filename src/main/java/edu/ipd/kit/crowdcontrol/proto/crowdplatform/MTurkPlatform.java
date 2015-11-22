package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.ClientConfig;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<Hit> publishTask(Hit hit) {
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
            Objects.requireNonNull(hit1);
            return new Hit(hit1.getHITId(), hit.getTitle(), hit.getDescription(), hit.getTags(), hit.getAmount(), hit.getPayment(), hit.getAssignmentDuration(), hit.getHitDuration(), hit.getUrl());
        });
    }

    @Override
    public CompletableFuture<Hit> updateTask(Hit hit) {
        return CompletableFuture.supplyAsync(() -> {
            HIT mhit = service.getHIT(hit.getId());

            if (mhit == null) return null;

            int assignmentIncrement = (int) (hit.getAssignmentDuration() - mhit.getAssignmentDurationInSeconds());

            if (assignmentIncrement < 0) {
                System.err.println("Assignment duration has to be bigger");
                throw new IllegalStateException("something wrong");
            }

            String keywords = hit.getTags().stream()
                    .collect(Collectors.joining(","));

            try {
                service.extendHIT(hit.getId(), assignmentIncrement, (long) 0);
            } catch (ServiceException e) {
                e.printStackTrace();
                return null;
            }

            String id = null;
            try {
                id = service.updateHIT(hit.getId(), hit.getTitle(), hit.getDescription(), keywords, hit.getPayment());
            } catch (ServiceException e) {
                e.printStackTrace();
                return null;
            }

            return new Hit(id, hit.getTitle(), hit.getDescription(), hit.getTags(), hit.getAmount(), hit.getPayment(), hit.getAssignmentDuration(), hit.getHitDuration(), hit.getUrl());
        });
    }

    /**
     * unpublish the task, after this call no answers can be sent for this task
     *
     * @param id
     * @return true if successful, false if not
     */
    @Override
    public CompletableFuture<String> unpublishTask(String id) {
        HIT mhit = service.getHIT(id);

        if (mhit == null) {
            System.err.println("Hit is not found!");
            return CompletableFuture.completedFuture(id);
        }

        String[] hitids = {id};

        CompletableFuture<String> result = new CompletableFuture<>();
        service.deleteHITs(hitids, false, true, (o, b, o1, e) -> result.complete(id));
        return result;
    }

    @Override
    public CompletableFuture<Hit> payTask(Hit hit) {
        //
        return CompletableFuture.completedFuture(hit);
    }

    @Override
    public String getName() {
        return null;
    }
}
