package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TasksOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class handles managing of the platforms.
 *
 * The class will update the list of known platforms in the database.
 * c
 * Created by marcel on 19.01.16.
 */
public class PlatformManager {
    private final Map<String, Platform> platforms;
    private final Worker fallbackWorker;
    private final Payment fallbackPayment;
    private TasksOperations tasksOps;

    public PlatformManager(List<Platform> crowdPlatforms, Worker fallbackWorker,
                           Payment fallbackPayment, TasksOperations tasksOps,
                           PlatformOperations platformOps) {
        this.tasksOps = tasksOps;
        this.fallbackWorker = fallbackWorker;
        this.fallbackPayment = fallbackPayment;


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

            /* platform does not handle payment, email is needed for internal payment */
            if (!platform.getPayment().isPresent())
                rec.setNeedsEmail(true);
            /* if platform cannot identify worker, we need to do that with a email adress */
            if (!platform.getWorker().isPresent())
                rec.setNeedsEmail(true);

            rec.setRenderCalibrations(platform.isCalibsAllowd());

            platformOps.createPlatform(rec);
        });
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
    public Optional<Worker> getWorker(String name) {
        return getPlatform(name).map(platform -> platform.getWorker().orElse(fallbackWorker));
    }

    /**
     * Will return the payment service which should be used for the given platform
     * If there is no Platform with the given name None is returned.
     *
     * @param name The name of the platform to use
     * @return The interface used for payment
     */
    public Optional<Payment> getPlatformPayment(String name) {
        return getPlatform(name).map(platform -> platform.getPayment().orElse(fallbackPayment));
    }

    /**
     * Publish the given experiment on the platform.
     * The method will update the database with the new public task
     *
     * @param name The name of the platform
     * @param experiment The experiment to publish
     * @return None if the platform does not exist
     */
    public Optional<CompletableFuture<Boolean>> publishTask(String name, Experiment experiment) {
        return getPlatform(name).
                map(platform1 -> platform1.publishTask(experiment)).
                map(stringCompletableFuture -> stringCompletableFuture.handle((s, throwable) -> {
                    if (s != null && throwable == null) {
                        TaskRecord record = new TaskRecord();
                        record.setExperiment(experiment.getId());
                        record.setPlatformData(s);
                        record.setStatus(TaskStatus.running);
                        record.setCrowdPlatform(name);
                        tasksOps.createTask(record);
                    }
                    return true;
                }));
    }

    /**
     * Unpublish a given experiment from the given platform
     *
     * @param name The name of the platform
     * @param experiment The experiment to unpublish
     * @return None if the platform was not found, false if the unpublish failed and true if everything went fine
     */
    public Optional<CompletableFuture<Boolean>> unpublishTask(String name, Experiment experiment) {
        TaskRecord record;

        record = tasksOps.searchTask(name, experiment.getId()).orElse(null);
        if (record == null) return Optional.empty();

        return getPlatform(name).map(platform1 ->
            platform1.unpublishTask(record.getPlatformData()).handle((b, throwable) -> {
                if (b != null && throwable == null) {
                    record.setStatus(TaskStatus.finished);
                    tasksOps.updateTask(record);
                }
                return true;
            })
        );
    }

    /**
     * update the given experiment on the given platform
     * @param name The name of the platform
     * @param experiment The experiment to update
     * @return None if the platform was not found, false if the update failed and true if everything went fine.
     */
    public Optional<CompletableFuture<Boolean>> updateTask(String name, Experiment experiment) {
        TaskRecord record;

        record = tasksOps.searchTask(name, experiment.getId()).orElse(null);
        if (record == null) return Optional.empty();

        return getPlatform(name).
                map(platform -> platform.updateTask(record.getPlatformData(), experiment)).
                map(stringCompletableFuture -> {
                    CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
                    stringCompletableFuture.whenComplete((s, throwable) -> {
                        if (s != null && throwable == null) {
                            record.setPlatformData(s);
                            tasksOps.updateTask(record);
                        }
                        result.complete(true);
                    });
                    return result;
        });
    }

    /**
     * Parse a worker id out of the params which got passed by a platform
     * @param name The name of the platform
     * @param params Params passed by the platform
     * @return A String if the platform exists
     */
    public Optional<String> getWorkerId(String name, Map<String, String[]> params) {
        return getWorker(name).flatMap(worker -> worker.getWorkerId(params));
    }
}
