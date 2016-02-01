package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.Platform;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentification;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


/**
 * Created by marcel on 31.01.16.
 */
public class MturkPlatform implements Platform,Payment,WorkerIdentification {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * This calculates the request signature as described here:
     *
     * http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMechanicalTurkRequester/MakingRequests_RequestAuthenticationArticle.html
     *
     * @param service The service which is used
     * @param operation The operation which is called
     * @param timestamp The current time
     * @param secretAccessKey The key of the accessor
     * @return a request signature
     */
    private String calculateRequestSignature(String service, String operation, String timestamp, String secretAccessKey) throws NoSuchAlgorithmException, InvalidKeyException {

        SecretKeySpec signingKey = new SecretKeySpec(secretAccessKey.getBytes(),
                HMAC_SHA1_ALGORITHM);

        // get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);

        // compute the hmac on input data bytes
        byte[] rawHmac = mac.doFinal((service+operation+timestamp).getBytes());

        // base64-encode the hmac
        return new String(Base64.getEncoder().encode(rawHmac));
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
