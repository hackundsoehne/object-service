package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.amazonaws.mturk.requester.doc._2014_08_15.Errors;
import com.amazonaws.mturk.requester.doc._2014_08_15.KeyValuePair;
import com.amazonaws.mturk.requester.doc._2014_08_15.Request;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 * Utils class for mturk/rest/untils
 */
public class Utils {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * Transforms a request into a exception if the request is invalid
     * @param request request to handle the error in case
     * @throws RuntimeException for the case the request is invalid
     */
    public static void handleRequest(Request request) throws RequestException {
        if (request.getIsValid().equals("False")) {
            StringBuilder builder = new StringBuilder();

            for (Errors.Error error : request.getErrors().getError()) {
                builder.append(error.getCode());
                builder.append(" : ");
                builder.append(error.getMessage());
                builder.append("\n");
                for (KeyValuePair pair : error.getData()) {
                    builder.append(pair.getKey()).append(" = ").append(pair.getValue());
                }
            }
            throw new RequestException(builder.toString());
        }
    }
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
    public static String calculateRequestSignature(String service, String operation, String timestamp, String secretAccessKey) throws NoSuchAlgorithmException, InvalidKeyException {

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
}
