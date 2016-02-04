package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.amazonaws.mturk.requester.doc._2014_08_15.Assignment;
import com.amazonaws.mturk.requester.doc._2014_08_15.AssignmentStatus;
import com.amazonaws.mturk.requester.doc._2014_08_15.BonusPayment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Platform implementation for a mturk instance
 * @author MarcelHollerbach
 * @version 0.1
 */
public class MturkPlatform implements Platform,Payment,WorkerIdentification {

    private final MTurkConnection connection;

    /**
     * A new mturk platform instance
     * @param user user to login
     * @param password password to use
     * @param url instance to connect to
     */
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
                experiment.getPaymentBase().getValue()/100, //we are getting cents passed and have to pass dallers
                60*60*24, //you have 24 hours to do the assignment
                60*60*24*31*12, // the experiment is staying for ONE year
                tags,
                experiment.getNeededAnswers().getValue()*experiment.getRatingsPerAnswer().getValue(),
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
        Map<String, BigDecimal> bonusPayed = new HashMap<>();
        List<CompletableFuture<Boolean>> jobs = new ArrayList<>();

        try {
            int i = 0;
            //first get a hashmap of all assignmentids and worker ids
            List<Assignment> assignmentList = new GetAssignments(connection,id,1).get();
            while (assignmentList.size() > 0) {
                for(Assignment assignment : assignmentList) {
                    workerAssignmentId.put(assignment.getWorkerId(), assignment);
                }
                i++;
                assignmentList = new GetAssignments(connection,id,i).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            CompletableFuture<Boolean> completableFuture= new CompletableFuture<>();
            completableFuture.completeExceptionally(e);
            return completableFuture;
        }

        //verify and get all data
        verifyConsistence(id, paymentJobs, workerAssignmentId, bonusPayed);

        //do the real paying
        flushPayment(experiment, paymentJobs, workerAssignmentId, bonusPayed, jobs);

        //concat all future objects to one big object
        return CompletableFuture.supplyAsync(() ->
                jobs.stream().map(CompletableFuture::join)
                .filter(aBoolean -> !aBoolean).count() == 0);
    }

    /**
     * Approve a assignment of a payment job if it is not yet and pay the bonus (or the rest of bonus) which is left
     * @param experiment experiment to pay
     * @param paymentJobs a list of payment jobs
     * @param workerAssignmentId all assignments
     * @param bonusPayed payed bonuses per assignment
     * @param jobs list of jobs where to append new completables
     */
    private void flushPayment(Experiment experiment, List<PaymentJob> paymentJobs, Map<String, Assignment> workerAssignmentId, Map<String, BigDecimal> bonusPayed, List<CompletableFuture<Boolean>> jobs) {
        for(PaymentJob paymentJob : paymentJobs) {
            Assignment assignment =
                    workerAssignmentId.get(paymentJob.getWorkerRecord().getIdentification());
            //check if we should pay at all
            if (paymentJob.getAmount() < experiment.getPaymentBase().getValue()) {
                //amount is smaller than payment base ? REJECT!
                jobs.add(new RejectAssignment(connection,assignment.getAssignmentId(),
                        "You answer did not match the wanted rating criteria"));
            }else {
                //pay the worker regular
                int amount = paymentJob.getAmount() - experiment.getPaymentBase().getValue();
                //approve the assignment if it is not right now
                if (assignment.getAssignmentStatus().equals(AssignmentStatus.SUBMITTED)) {
                    //approving here triggers base payment
                    jobs.add(new ApproveAssignment(connection,assignment.getAssignmentId(),
                            "Thx for passing your answer!"));
                    jobs.add(payBonus(paymentJob, assignment, amount));
                } else if (assignment.getAssignmentStatus().equals(AssignmentStatus.APPROVED)) {
                    //the assignment is already approved
                    //check if a bonus was payed
                    BigDecimal payedBonus = bonusPayed.get(assignment.getAssignmentId());
                    double dollars = amount / 100;

                    //check if we payed enough bonus
                    if (payedBonus.doubleValue() < dollars) {
                        //we need to pay the rest of the bonus
                        jobs.add(payBonus(paymentJob, assignment, ((int)(dollars - payedBonus.doubleValue())*100)));
                    }
                }
            }
        }
    }

    /**
     * verify if the map of assignments contains a assignment for all payment Jobs
     *
     * and if a assignment was already approved check how much bonus we need to pay left
     *
     * @param id if of the hit
     * @param paymentJobs payment jobs
     * @param workerAssignmentId workerid to assignment id map
     * @param bonusPayed map which gives every assignmentid a amount of payed bonus
     * @throws IllegalArgumentException
     */
    private void verifyConsistence(String id, List<PaymentJob> paymentJobs, Map<String, Assignment> workerAssignmentId, Map<String, BigDecimal> bonusPayed) throws IllegalArgumentException {
        //check if each passed worker got a assignment id
        for(PaymentJob paymentJob : paymentJobs) {
            Assignment assignment = workerAssignmentId.get(paymentJob.getWorkerRecord().getIdentification());
            //check if each worker gets a assignment
            if (assignment == null) {
                throw new IllegalArgumentException("Worker "+paymentJob.getWorkerRecord().getIdentification()+" does not have a assignment id");
            }
            //check if assignments are already submitted
            if (assignment.getAssignmentStatus().equals(AssignmentStatus.APPROVED)) {

                //try to get bonus payments information
                BigDecimal bonusAmount = new BigDecimal(0);
                try {
                    List<BonusPayment> bonusPayment = new GetBonusPayments(connection, id, assignment.getAssignmentId(), 1).get();
                    //calculate how much bonus we payed
                    for (BonusPayment bonusPayment1 : bonusPayment)
                        bonusAmount = bonusAmount.add(bonusPayment1.getBonusAmount().getAmount());
                    //mark it in the map
                    bonusPayed.put(assignment.getAssignmentId(), bonusAmount);
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalArgumentException("Failed to fetch Bonus Payments information", e);
                }
            }
        }
    }

    private CompletableFuture<Boolean> payBonus(PaymentJob paymentJob, Assignment assignment, int amount) {
        //if there is money left pay a bonus
        if (amount > 0) {
            new GrantBonus(connection,assignment.getAssignmentId(),
                       paymentJob.getWorkerRecord().getIdentification(),amount/100,"This is the bonus for a high rating!");
        }
        return CompletableFuture.completedFuture(true);
    }
}
