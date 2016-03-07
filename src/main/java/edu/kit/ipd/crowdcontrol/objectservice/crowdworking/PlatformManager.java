package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsPlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class handles managing of the platforms.
 * Created by marcel on 19.01.16.
 */
public class PlatformManager {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private final Map<String, Platform> platforms;
    private final Function<String, WorkerIdentificationComputation> fallbackWorker;
    private final Payment fallbackPayment;
    private ExperimentsPlatformOperations experimentsPlatformOps;
    private WorkerOperations workerOps;

    /**
     * Create a new manager for platforms. The known platforms in the database will be deleted,
     * and filled with the new.
     *
     * @param crowdPlatforms The list of crowdplatforms to be managed by this manager,
     *                       will be used to setup the list of platforms in the database
     * @param fallbackWorker handler which is called if a platform does not support identifying a worker
     *                       for this case need_email on the platform is set and the email which got entered by the worker
     *                       should be set as some param. The function takes the name of the platform.
     * @param fallbackPayment handler which is called if a platform does not support payment
     * @param experimentsPlatformOps Used for the experimentsPlatform operations on the database
     * @param platformOps Used for the platform operations on the database
     * @param workerOps Used for the worker operations on the database
     */
    public PlatformManager(List<Platform> crowdPlatforms, Function<String, WorkerIdentificationComputation> fallbackWorker,
                           Payment fallbackPayment, ExperimentsPlatformOperations experimentsPlatformOps,
                           PlatformOperations platformOps, WorkerOperations workerOps) {
        this.experimentsPlatformOps = experimentsPlatformOps;
        this.fallbackWorker = fallbackWorker;
        this.fallbackPayment = fallbackPayment;
        this.workerOps = workerOps;

        //create hashmap of platforms
        platforms = crowdPlatforms.stream()
                .collect(Collectors.toMap(Platform::getID, Function.identity()));

        //update database
        List<PlatformRecord> records = platforms.values().stream()
                .map(platform -> new PlatformRecord(
                        platform.getID(),
                        platform.getName(),
                        platform.isCalibrationAllowed(),
                        isNeedemail(platform),
                        false
                ))
                .collect(Collectors.toList());

        platformOps.storePlatforms(records);
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
     * Will return the payment service which should be used for the given platform
     * If there is no Platform with the given name None is returned.
     *
     * @param name The name of the platform to use
     * @return The interface used for payment
     */
    public Payment getPlatformPayment(String name) {
        return getPlatformOrThrow(name)
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
    //TODO improve doc: what is none? Completable with null (bad api) what does true/false mean? or does it return null?
    public CompletableFuture<Boolean> publishTask(String name, Experiment experiment) throws TaskOperationException {
        ExperimentsPlatformRecord record = experimentsPlatformOps.getExperimentsPlatform(name, experiment.getId()).
                orElseThrow(() -> new IllegalStateException("Platform is not activated for experiment " + experiment));

        BiFunction<String, Throwable, Boolean> handlePublishResult = (s1, throwable) -> {
            //if the creation was successful update the db
            if (s1 != null && throwable == null && !s1.isEmpty()) {
                experimentsPlatformOps.setPlatformStatus(record.getIdexperimentsPlatforms(),
                        ExperimentsPlatformStatusPlatformStatus.running);
                record.setPlatformData(s1);
                experimentsPlatformOps.updateExperimentsPlatform(record);
                if (!experimentsPlatformOps.updateExperimentsPlatform(record)) {
                    getPlatformOrThrow(name).unpublishTask(record.getPlatformData())
                            .join();
                    throw new IllegalStateException("Updating record for published experimentsPlatform failed");
                }
            }
            //if there is no useful key throw!
            if (s1 == null || (s1.isEmpty())) {
                try {
                    unpublishTask(name, experiment);
                } catch (TaskOperationException e) {
                    LOGGER.error("Platform " + name + " does not provide any useful key and has thrown an " +
                            "exception when tried to unpublish the task", e);
                }
                experimentsPlatformOps.setPlatformStatus(record.getIdexperimentsPlatforms(),
                        ExperimentsPlatformStatusPlatformStatus.running);
                throw new IllegalStateException("Platform " + name + " does not provide any useful key");
            }
            //if not rethrow the exception and update the db
            if (throwable != null) {
                experimentsPlatformOps.setPlatformStatus(record.getIdexperimentsPlatforms(),
                        ExperimentsPlatformStatusPlatformStatus.failedPublishing);

                throw new RuntimeException(throwable);
            }
            return true;
        };

        return getPlatformOrThrow(name)
                .publishTask(experiment)
                .handle(handlePublishResult);
    }

    /**
     * Unpublishes a given experiment from the given platform
     *
     * @param name The name of the platform
     * @param experiment The experiment to unpublish
     * @return None if the platform was not found, false if the unpublish failed and true if everything went fine
     */
    public CompletableFuture<Boolean> unpublishTask(String name, Experiment experiment) throws TaskOperationException {
        ExperimentsPlatformRecord record = experimentsPlatformOps.getExperimentsPlatform(name, experiment.getId())
                .orElse(null);

        if (record == null)
            return CompletableFuture.completedFuture(true);

        return getPlatformOrThrow(name).unpublishTask(record.getPlatformData())
                .thenApply(aBoolean -> {
                    //TODO: marcel why do you ignore the return type of unpublishTask?
                    experimentsPlatformOps.setPlatformStatus(record.getIdexperimentsPlatforms(),
                            ExperimentsPlatformStatusPlatformStatus.finished);
                    return true;
                });
    }

    private Platform getPlatformOrThrow(String name) {
        return getPlatform(name)
                .orElseThrow(() -> new IllegalArgumentException("Platform \""+name+"\" not found"));
    }

    /**
     * Parse a worker id out of the params which got passed by a platform
     * @param name The name of the platform
     * @param params Params passed by the platform
     * @return A String if the platform exists
     * @throws UnidentifiedWorkerException if passed invalid params
     */
    public WorkerIdentification identifyWorker(String name, Map<String, String[]> params) throws UnidentifiedWorkerException {
        return getPlatformOrThrow(name)
                .getWorker()
                .orElseGet(() -> fallbackWorker.apply(name))
                .getWorker(params);
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
     * @return a future object which indicates if the payment was successful or not
     * @throws IllegalWorkerSetException if this exception is thrown NO payment requests are given to the platform
     *
     */
    public CompletableFuture<Boolean> payExperiment(String name, Experiment experiment, List<PaymentJob> paymentJobs) throws TaskOperationException, IllegalWorkerSetException {
        ExperimentsPlatformRecord record = experimentsPlatformOps.getExperimentsPlatform(name, experiment.getId()).
                orElseThrow(() -> new TaskOperationException("Platform is not activated for experiment "+experiment));
        List<WorkerRecord> workerRecords = workerOps.getWorkerWithWork(experiment.getId(), name);

        Set<String> given = paymentJobs.stream().map(paymentJob -> paymentJob.getWorkerRecord().getIdentification()).collect(Collectors.toSet());
        Set<String> should = workerRecords.stream().map(WorkerRecord::getIdentification).collect(Collectors.toSet());

        if (!given.equals(should)) {
            throw new IllegalWorkerSetException(
                    "The list of payment Jobs need to have all workers which worked on this experiment on the given platform");
        }

        return getPlatformPayment(name).payExperiment(record.getPlatformData(), experiment,paymentJobs);
    }
}
