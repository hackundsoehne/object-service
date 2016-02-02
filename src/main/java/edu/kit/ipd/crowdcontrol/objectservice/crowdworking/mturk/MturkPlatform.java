package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.mashape.unirest.http.Unirest;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


/**
 * Created by marcel on 31.01.16.
 */
public class MturkPlatform implements Platform,Payment,WorkerIdentification {

    public MturkPlatform() {

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

        return null;
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(String id) {
        return null;
    }

    @Override
    public CompletableFuture<String> updateTask(String id, Experiment experiment) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> payWorker(Worker worker, int amount) {
        return null;
    }

    @Override
    public String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        return null;
    }
}
