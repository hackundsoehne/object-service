package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TasksOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class handles managing of the platforms.
 * Created by marcel on 19.01.16.
 */
public class PlatformManager {
    private final Map<String, Platform> platforms;
    private final WorkerIdentification fallbackWorker;
    private final Payment fallbackPayment;
    private TasksOperations tasksOps;
    private WorkerOperations workerOps;


    /**
     * Create a new manager for platforms. The known platforms in the database will be deleted,
     * and filled with the new.
     *
     * @param crowdPlatforms The list of crowdplatforms to be managed by this manager,
     *                       will be used to setup the list of platforms in the database
     * @param fallbackPayment handler which is called if a platform does not support payment
     * @param tasksOps Used for the task operations on the database
     * @param platformOps Used for the platform operations on the database
     * @param workerOps Used for the worker operations on the database
     */
    public PlatformManager(List<Platform> crowdPlatforms, Payment fallbackPayment, TasksOperations tasksOps,
                           PlatformOperations platformOps, WorkerOperations workerOps) {
        this(crowdPlatforms, new FallbackWorker(), fallbackPayment, tasksOps, platformOps, workerOps);
    }

    /**
     * Create a new manager for platforms. The known platforms in the database will be deleted,
     * and filled with the new.
     *
     * @param crowdPlatforms The list of crowdplatforms to be managed by this manager,
     *                       will be used to setup the list of platforms in the database
     * @param fallbackWorker handler which is called if a platform does not support identifying a worker
     *                       for this case need_email on the platform is set and the email which got entered by the worker
     *                       should be set as some param
     * @param fallbackPayment handler which is called if a platform does not support payment
     * @param tasksOps Used for the task operations on the database
     * @param platformOps Used for the platform operations on the database
     * @param workerOps Used for the worker operations on the database
     */
    public PlatformManager(List<Platform> crowdPlatforms, WorkerIdentification fallbackWorker,
                           Payment fallbackPayment, TasksOperations tasksOps,
                           PlatformOperations platformOps, WorkerOperations workerOps) {
        this.tasksOps = tasksOps;
        this.fallbackWorker = fallbackWorker;
        this.fallbackPayment = fallbackPayment;
        this.workerOps = workerOps;

        //create hashmap of platforms
        platforms = crowdPlatforms.stream()
                .collect(Collectors.toMap(Platform::getName, Function.identity()));
        //clear database
        platformOps.deleteAllPlatforms();
        //update database
        platforms.forEach((s, platform) -> {
            PlatformRecord rec = new PlatformRecord();
            rec.setName(platform.getName());
            rec.setNeedsEmail(false);

            rec.setNeedsEmail(isNeedemail(platform));
            rec.setRenderCalibrations(platform.isCalibrationAllowed());

            platformOps.createPlatform(rec);
        });
    }

    private boolean isNeedemail(Platform platform) {
        boolean needemail = false;

        /* platform does not handle payment, email is needed for internal payment */
        if (!platform.getPayment().isPresent())
            needemail = true;

        /* if platform cannot identify worker, we need to do that with a email adress */
        if (!platform.getWorker().isPresent())
            needemail = true;

        return needemail;
    }

    /**
     * Returns if the given Platform needs a email or not
     * @param name name of the platform
     * @return true if the platform needs an email, false if not
     */
    public boolean getNeedemail(String name) {
        return isNeedemail(
                getPlatform(name).orElseThrow(() -> new IllegalArgumentException("Platform not found"))
        );
    }

    /**
     * Will get you the instance of a platform interface of a platform, this instance is the same for all calls
     * @param name The name of the instance to use
     * @return The optional crowd platform instance
     */
    public Optional<Platform> getPlatform(String name) {
        return Optional.ofNullable(platforms.get(name));
    }

    /**
     * Will return the Worker interface which should be used to identify workers for the given platform
     *
     * @param name The name of the platform
     * @return The interface used to identify a worker
     */
    public WorkerIdentification getWorker(String name) {
        return getPlatform(name)
                .orElseThrow(() -> new IllegalArgumentException("Platform not found"))
                .getWorker().orElse(fallbackWorker);
    }

    /**
     * Will return the payment service which should be used for the given platform
     * If there is no Platform with the given name None is returned.
     *
     * @param name The name of the platform to use
     * @return The interface used for payment
     */
    public Payment getPlatformPayment(String name) {
        return getPlatform(name)
                .orElseThrow(() -> new IllegalArgumentException("Platform not found"))
                .getPayment().orElse(fallbackPayment);
    }

    /**
     * Publish the given experiment on the platform.
     * The method will update the database with the new public task
     *
     * If a exception is thrown NO TaskRecord is created, this means the Experiment will still be known as "unpublished"
     *
     * @param name The name of the platform
     * @param experiment The experiment to publish
     * @return None if the platform does not exist
     */
    public CompletableFuture<Boolean> publishTask(String name, Experiment experiment) throws TaskOperationException {

        TaskRecord record = new TaskRecord();
        record.setExperiment(experiment.getId());
        record.setStatus(TaskStatus.running);
        record.setCrowdPlatform(name);

        TaskRecord result = tasksOps.createTask(record);
        if (result == null)
            throw new TaskOperationException("Task could not be created");

        return getPlatform(name)
                .map(platform1 -> platform1.publishTask(experiment))
                .orElseThrow(() -> new IllegalArgumentException("Platform not found!"))
                .handle((s1, throwable) -> {
                    //if the creation was successful update the task
                    if (s1 != null && throwable == null && !s1.isEmpty()) {
                        result.setPlatformData(s1);
                        if (!tasksOps.updateTask(result)) {
                            throw new IllegalStateException("Updating record for published task failed");
                        }
                    }
                    //if there is no useful key throw!
                    if (s1 != null && s1.isEmpty()) {
                        tasksOps.deleteTask(result);
                        throw new IllegalStateException("Platform "+name+" does not provide any useful key");
                    }
                    //if not rethrow the exception and delete the task
                    if (throwable != null) {
                        tasksOps.deleteTask(result);
                        throw new RuntimeException(throwable);
                    }
                    return true;
                });
    }

    /**
     * Unpublish a given experiment from the given platform
     *
     * @param name The name of the platform
     * @param experiment The experiment to unpublish
     * @return None if the platform was not found, false if the unpublish failed and true if everything went fine
     */
    public CompletableFuture<Boolean> unpublishTask(String name, Experiment experiment) throws TaskOperationException {
        TaskRecord record;

        record = tasksOps.getTask(name, experiment.getId()).orElse(null);

        if (record == null)
            return CompletableFuture.completedFuture(true);

        return getPlatform(name).map(platform -> platform.unpublishTask(record.getPlatformData()))
                .orElseThrow(() -> new IllegalArgumentException("Experiment not found!"))
                .thenApply(aBoolean -> {
                    record.setStatus(TaskStatus.finished);
                    return tasksOps.updateTask(record);
                });
    }

    /**
     * update the given experiment on the given platform
     * @param name The name of the platform
     * @param experiment The experiment to update
     * @return None if the platform was not found, false if the update failed and true if everything went fine.
     */
    public CompletableFuture<Boolean> updateTask(String name, Experiment experiment) throws TaskOperationException {
        TaskRecord record;

        record = tasksOps.getTask(name, experiment.getId()).
                orElseThrow(() -> new TaskOperationException("Experiment is not published"));

        return getPlatform(name)
                .map(platform -> platform.updateTask(record.getPlatformData(), experiment))
                .orElseThrow(() -> new IllegalArgumentException("Platform not found"))
                .thenApply(s -> {
                    record.setPlatformData(s);
                    return tasksOps.updateTask(record);
                });
    }

    /**
     * Parse a worker id out of the params which got passed by a platform
     * @param name The name of the platform
     * @param params Params passed by the platform
     * @return A String if the platform exists
     * @throws UnidentifiedWorkerException if the user can not be found by the platform code
     */
    public String identifyWorker(String name, Map<String, String[]> params) throws UnidentifiedWorkerException {
        return getWorker(name).identifyWorker(params);
    }

    /**
     * Get a worker if he exists
     * @param name Name of the platform
     * @param params Params passed by the platform
     * @return A worker if one is found
     * @throws UnidentifiedWorkerException if the platform does not identify a worker
     */
    public Optional<WorkerRecord> getWorker(String name, Map<String ,String[]> params) throws UnidentifiedWorkerException {
        return getWorker(name).getWorker(workerOps,name,params);
    }

    /**
     * Pay all worker of a experiment
     *
     * The passed list of paymentJobs contains all worker which have submitted a answer.
     * If the worker should not get payed because of bad ratings the amount should be smaller
     * than the basepayment of the experiment.
     *
     * @param name name of the platform
     * @param experiment experiment which is published
     * @param paymentJobs tuples which are defining the amount to pay
     * @return
     */
    public CompletableFuture<Boolean> payExperiment(String name, Experiment experiment, List<PaymentJob> paymentJobs) throws TaskOperationException {
        TaskRecord record = tasksOps.getTask(name, experiment.getId()).
                orElseThrow(() -> new TaskOperationException("Experiment was never published"));

        return getPlatformPayment(name).payExperiment(record.getPlatformData(), experiment,paymentJobs);
    }
}
