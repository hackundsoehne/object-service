package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.amazonaws.mturk.requester.doc._2014_08_15.Assignment;
import com.amazonaws.mturk.requester.doc._2014_08_15.AssignmentStatus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.kit.ipd.crowdcontrol.objectservice.Utils;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.*;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Platform implementation for a mturk instance
 * @author MarcelHollerbach
 * @version 0.1
 */
public class MturkPlatform implements Platform,Payment {
    public static final int THIRTY_DAYS = 60 * 60 * 24 * 30;
    public static final int TWO_HOURS = 60 * 60 * 2;
    private final String name;
    private final MTurkConnection connection;
    private final String workerServiceUrl;
    private final String workerUIUrl;
    private HitExtender hitExtender;
    /**
     * A new mturk platform instance
     * @param user user to login
     * @param password password to use
     * @param url instance to connect to
     * @param workerUIUrl path where to find the workerUI
     */
    public MturkPlatform(String user, String password, String url, String name, String workerServiceUrl, String workerUIUrl) {
        connection = new MTurkConnection(user, password, url);
        this.workerUIUrl = workerUIUrl;
        this.workerServiceUrl = workerServiceUrl;
        this.name = name;
    }

    public void startExtenderService(List<String> hits) {
        this.hitExtender = new HitExtender(hits, connection);
    }

    @Override
    public String getName() {
        return "Mturk "+name;
    }

    @Override
    public String getID() {
        return "mturk"+name.toLowerCase();
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
    public Optional<WorkerIdentificationComputation> getWorker() {
        return Optional.of(params -> {
            String workerId = "";
            if (params != null) {
                String[] workerIdArray = params.get("mTurkWorkerId");

                if (workerIdArray != null && workerIdArray.length > 0) {
                    workerId = workerIdArray[0];
                } else {
                    throw new UnidentifiedWorkerException("mTurkWorkerId was not set!");
                }
            }

            return WorkerIdentification.findByIdentification(getID(),workerId);
        });
    }

    @Override
    public CompletableFuture<JsonElement> publishTask(Experiment experiment) {
        String tags = experiment.getTagsList().stream().map(Tag::getName).collect(Collectors.joining(","));
        String jsContent = Utils.loadFile("/mturk/worker-ui/mturk.js");
        String htmlContent = Utils.loadFile("/mturk/worker-ui/MturkContent.html");

        Map<String, String> params = new HashMap<>();
        params.put("PlatformName", getID());
        params.put("WorkerServiceUrl", workerServiceUrl);
        params.put("WorkerUIUrl", workerUIUrl);
        params.put("ExperimentId", experiment.getId()+"");
        params.put("JsEmbed", jsContent);

        String content = Template.apply(htmlContent, params);

        return new PublishHIT(connection,experiment.getTitle(),experiment.getDescription(),
                experiment.getPaymentBase().getValue()/100d, //we are getting cents passed and have to pass dollars
                TWO_HOURS, //you have 2 hours to do the assignment
                THIRTY_DAYS, // the experiment is staying for 30 days
                tags,
                1000000000,
                2592000, //this is a little problem we have to specify when autoapproval is kicking in this is happening after 2592000s
                "",
                content)
                .thenApply(id -> {
                    if (hitExtender != null)
                      hitExtender.addHit(id);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("identification", new JsonPrimitive(id));
                    return jsonObject;
                });
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(JsonElement data) {
        String id = data.getAsJsonObject().get("identification").getAsString();
        if (hitExtender != null)
          hitExtender.removeHit(id);
        return new UnpublishHIT(connection, id);
    }

    /**
     * Returns the full list of a paginated request
     * @param producer execute the statement and returns a patial list of the complete
     * @param <A> Type of the list
     * @return The full list of elements
     * @throws ExecutionException If something bad happens while executing
     * @throws InterruptedException the execution was interrupted
     */
    public <A> List<A> getFullList(Function<Integer, CompletableFuture<List<A>>> producer) throws ExecutionException, InterruptedException {
        int i = 1;
        List<A> part = producer.apply(i).get();
        List<A> complete = new ArrayList<>();
        while(part != null && part.size() != 0) {
            complete.addAll(part);
            i++;
            part = producer.apply(i).get();
        }
        return complete;
    }

    @Override
    public CompletableFuture<Boolean> payExperiment(int dbId, JsonElement data, Experiment experiment, List<PaymentJob> paymentJobs) {
        /**
         * this code works under the assumation that basepayment is part of the amout!!!!!!
         */
        Map<String, Assignment> workerAssignmentId = new HashMap<>();
        Map<String, BigDecimal> bonusPayed = new HashMap<>();
        String id = data.getAsJsonObject().get("identification").getAsString();
        try {
            // get all assignments from the project and sort them to the function
            getFullList( index -> new GetAssignments(connection, id, index))
                    .forEach(assignment -> workerAssignmentId.put(assignment.getWorkerId(), assignment));

            //get all done payments
            getFullList(index -> new GetBonusPayments(connection, id, index))
                    .forEach(bonusPayment -> {
                        BigDecimal bigDecimal = bonusPayed.get(bonusPayment.getWorkerId());
                        if (bigDecimal != null) {
                            bigDecimal = bigDecimal.add(bonusPayment.getBonusAmount().getAmount());
                        } else {
                            bigDecimal = bonusPayment.getBonusAmount().getAmount();
                        }
                        bonusPayed.put(bonusPayment.getWorkerId(), bigDecimal);
                    });

        } catch (InterruptedException | ExecutionException e) {
            CompletableFuture<Boolean> completableFuture= new CompletableFuture<>();
            completableFuture.completeExceptionally(e);
            return completableFuture;
        }

        //verify and get all data
        verifyConsistence(id, paymentJobs, workerAssignmentId, bonusPayed);

        //do the real paying
        return flushPayment(experiment, paymentJobs, workerAssignmentId, bonusPayed);
    }

    @Override
    public int getCurrency() {
        //USD
        return 840;
    }

    /**
     * Approve a assignment of a payment job if it is not yet and pay the bonus (or the rest of bonus) which is left
     * @param experiment experiment to pay
     * @param paymentJobs a list of payment jobs
     * @param workerAssignmentId all assignments
     * @param bonusPayed payed bonuses per assignment
     */
    private CompletableFuture<Boolean> flushPayment(Experiment experiment, List<PaymentJob> paymentJobs, Map<String, Assignment> workerAssignmentId, Map<String, BigDecimal> bonusPayed) {
        List<CompletableFuture<Boolean>> jobs = new ArrayList<>();

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
                int restAmount = paymentJob.getAmount() - experiment.getPaymentBase().getValue();
                //approve the assignment if it is not right now
                if (assignment.getAssignmentStatus().equals(AssignmentStatus.SUBMITTED)) {
                    //approving here triggers base payment
                    jobs.add(new ApproveAssignment(connection,assignment.getAssignmentId(),
                            "Thx for passing your answer!"));
                    jobs.add(payBonus(paymentJob, assignment, restAmount));
                    jobs.add(notifyWorker(paymentJob));
                } else if (assignment.getAssignmentStatus().equals(AssignmentStatus.APPROVED)) {
                    //the assignment is already approved
                    //check if a bonus was payed
                    BigDecimal payedBonus = bonusPayed.get(assignment.getWorkerId());
                    double should = restAmount / 100d;

                    if (payedBonus == null)
                        payedBonus = new BigDecimal(0.0);

                    double difference = should - payedBonus.doubleValue();

                    System.out.println("We should "+should+" diff: "+ difference+"i");

                    //only sent the mail if there was no payedBonus yet
                    if (payedBonus.compareTo(new BigDecimal(0.001d)) > 0) {
                        //there will be something payed
                        jobs.add(notifyWorker(paymentJob));
                    }

                    //check if we payed enough bonus
                    if (difference > 0) {
                        //we need to pay the rest of the bonus
                        jobs.add(payBonus(paymentJob, assignment, (int) (difference*100)));
                    }
                }
            }
        }
        return CompletableFuture.supplyAsync(() ->
                jobs.stream().map(CompletableFuture::join)
                        .allMatch(Boolean::booleanValue));
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
        }
    }

    /**
     * This will notify the worker from the job with the given message from the job
     *
     * If the message is longer than 4096 symbols the message is slitted in multiple messages
     * @param paymentJob the job to pay
     *
     * @return A future object that completes when all messages are sent.
     */
    private CompletableFuture<Boolean> notifyWorker(PaymentJob paymentJob) {
        int length = paymentJob.getMessage().length();
        int current_location = 0;
        int counter = 0;
        List<CompletableFuture<Boolean>> messages = new ArrayList<>();

        String tooLongMessage = Utils.loadFile("/mturk/TooLongMessage.txt");
        String subjectLine = Utils.loadFile("/mturk/SubjectLine.txt");
        //while loop sents messages if needed message would be gibber than MAX_LENGTH
        while(length - current_location > NotifyWorker.MAX_LENGTH) {
            int old_current_location = current_location;
            //the new location is the maximum - the too long message
            current_location = current_location + (NotifyWorker.MAX_LENGTH - tooLongMessage.length());

            String message = paymentJob.getMessage()
                    .substring(old_current_location, current_location) + "\n"+tooLongMessage;

            String subject = String.format(subjectLine, counter);

            messages.add(new NotifyWorker(connection, paymentJob.getWorkerRecord().getIdentification(), subject, message));

            counter ++;
        }
        messages.add(new NotifyWorker(connection, paymentJob.getWorkerRecord().getIdentification(),
                String.format(subjectLine, counter), paymentJob.getMessage().substring(current_location, length)));

        return CompletableFuture.supplyAsync(() ->
                messages.stream()
                .map(CompletableFuture::join)
                        .allMatch(Boolean::booleanValue));
    }

    private CompletableFuture<Boolean> payBonus(PaymentJob paymentJob, Assignment assignment, int amount) {
        String bonusMessage = Utils.loadFile("/mturk/BonusMessage.txt");
        //if there is money left pay a bonus
        if (amount >= 0) {
            return new GrantBonus(connection,assignment.getAssignmentId(),
                       paymentJob.getWorkerRecord().getIdentification(),(amount/100d), bonusMessage);
        } else {
            return CompletableFuture.completedFuture(true);
        }

    }
}
