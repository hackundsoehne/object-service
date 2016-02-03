package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.amazonaws.mturk.requester.doc._2014_08_15.Assignment;
import com.amazonaws.mturk.requester.doc._2014_08_15.AssignmentStatus;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Created by marcel on 31.01.16.
 */
public class MturkPlatform implements Platform,Payment,WorkerIdentification {

    private final MTurkConnection connection;

    public MturkPlatform(String user, String password, String url) {
        connection = new MTurkConnection(user, password, url);
    }

    @Override
    public String getName() {
        return "Mturk";
    }

    @Override
    public Boolean isCalibrationAllowed() {
        return false;
    }

    @Override
    public Optional<Payment> getPayment() {
        return Optional.of(this);
    }

    @Override
    public Optional<WorkerIdentification> getWorker() {
        return Optional.of(this);
    }


    @Override
    public CompletableFuture<String> publishTask(Experiment experiment) {
        String tags = experiment.getTagsList().stream().map(Tag::getName).collect(Collectors.joining(","));
        return new PublishHIT(connection,experiment.getTitle(),experiment.getDescription(),
                experiment.getPaymentBase()/100, //we are getting cents passed and have to pass dallers
                60*60*24, //you have 24 hours to do the assignment
                60*60*24*31*12, // the experiment is staying for ONE year
                tags,
                experiment.getNeededAnswers()*experiment.getRatingsPerAnswer(),
                31*24*60*60, //this is a little problem we have to specify when autoapproval is kicking in this is happening after a month
                "");
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return new UnpublishHIT(connection, id);
    }

    @Override
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return null;
    }

    @Override
    public String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> payExperiment(String id, Experiment experiment, List<PaymentJob> paymentJobs) {
        /**
         * this code works under the assumation that basepayment is part of the amout!!!!!!
         */
        Map<String, Assignment> workerAssignmentId = new HashMap<>();
        List<CompletableFuture<Boolean>> jobs = new ArrayList<>();
        try {
            //first get a hashmap of all assignmentids and worker ids
            List<Assignment> assignmentList = new GetAssignments(connection,id,0).get();
            while (assignmentList.size() > 0) {
                for(Assignment assignment : assignmentList) {
                    workerAssignmentId.put(assignment.getWorkerId(), assignment);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        for(PaymentJob paymentJob : paymentJobs) {
            Assignment assignment = workerAssignmentId.get(paymentJob.getWorkerRecord().getIdentification());
            if (assignment == null) {
                //FIXME this is fatal!! we cannot pay a worker here!!
                continue;
            }
            //check if we should pay at all
            if (paymentJob.getAmount() < experiment.getPaymentBase()) {

                //amount is smaller than payment base ? REJECT!
                jobs.add(new RejectAssignment(connection,assignment.getAssignmentId(),"You answer did not match the wanted rating criteria"));
            }else {

                //basepayment is triggered by approve
                int amount = paymentJob.getAmount() - experiment.getPaymentBase();

                //apporve the assignment if it is not right now
                if (assignment.getAssignmentStatus().equals(AssignmentStatus.SUBMITTED)) {
                    //approving here triggers base payment
                    jobs.add(new ApproveAssignment(connection,assignment.getAssignmentId(),"Thx for passing your answer!"));
                }

                //if there is money left pay a bonus
                if (amount > 0) {
                    jobs.add(new BonusPayment(connection,assignment.getAssignmentId(),
                               paymentJob.getWorkerRecord().getIdentification(),amount/100,"This is the bonus for a high rating!"));
                }
            }
        }
        return CompletableFuture.supplyAsync(() ->
                jobs.stream().map(CompletableFuture::join)
                .filter(aBoolean -> !aBoolean).count() == 0);
    }
}
