package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import javax.xml.bind.JAXBContext;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * This class abstracts a unirest request to mturk
 * @param <T> The result of the request
 * @param <K> The Internal value which is returned by the request
 */
public class MturkRestCommand<T,K> extends CompletableFuture<T> implements Callback<String> {
    private final Class<K> klass;
    private final SimpleDateFormat simpleDateFormat;
    private final String awsSecretAccessKey;
    private final Function<K,T> transformer;

    /**
     * This will init and fire up a request to mturk
     * @param url address where to send the operation
     * @param operation  name of the operation
     * @param values key value pairs which should be added
     * @param klass class object of the class which is returned by mturk
     * @param awsSecretAccessKey access key to use
     * @param responseGroup responseGroup which is requested from mturk
     * @param version version of the api to use
     * @param transformer Called to validate and transform the parsed object
     */
    public MturkRestCommand(String url, String operation, Map<String, Object> values, Map<String, Object> staticDefaults, Class<K> klass, String awsSecretAccessKey, String responseGroup, String version, Function<K, T> transformer) {
        this.klass = klass;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.transformer = transformer;

        //must be 2005-01-31T23:59:59Z.
        simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            Unirest.get(url)
                    .queryString(staticDefaults)
                    .queryString(generateDefaults(operation,responseGroup,version))
                    .queryString(values)
                    .asStringAsync(this);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            completeExceptionally(new RuntimeException(e));
        }
    }

    /**
     * Generates the default parameters
     * @param operation current operation
     * @param responseGroup what should be returned
     * @param version the version of the api to use
     * @return key value map
     * @throws InvalidKeyException
     */
    private Map<String, Object> generateDefaults(String operation, String responseGroup, String version) throws InvalidKeyException, NoSuchAlgorithmException {
        Map<String, Object> result = new HashMap<>();

        result.put("Operation", operation);
        result.put("Signature", Utils.calculateRequestSignature("AWSMechanicalTurkRequester",
                operation, simpleDateFormat.format(new Date()),
                awsSecretAccessKey));

        result.put("Timestamp", simpleDateFormat.format(new Date()));
        result.put("ResponseGroup", responseGroup);
        result.put("Version", version);
        return result;
    }

    @Override
    public void completed(HttpResponse response) {
        try {
            //parse the result of the call
            JAXBContext context = JAXBContext.newInstance(klass);
            K k = (K) context.createUnmarshaller().unmarshal(response.getRawBody());
            //complete the completable future with the transformed object
            complete(transformer.apply(k));
        } catch (Exception e) {
            completeExceptionally(e);
        }
    }

    @Override
    public void failed(UnirestException e) {
        completeExceptionally(e);
    }

    @Override
    public void cancelled() {
        completeExceptionally(new IllegalStateException("Cancelled by unirest"));
    }
}
