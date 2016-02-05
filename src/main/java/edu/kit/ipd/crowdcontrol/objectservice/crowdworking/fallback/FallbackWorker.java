package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.fallback;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentification;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Map;

/**
 * The fallback-worker uses the email-address to identify workers.
 * @author LeanderK
 * @version 1.0
 */
public class FallbackWorker implements WorkerIdentification {
    /**
     * Parse a worker id out of the params
     *
     * @param param The parameters which were sent by a platform
     * @return The id of the worker if one can be found
     */
    @Override
    public String identifyWorker(Map<String, String[]> param) throws UnidentifiedWorkerException {
        //if the worker provides an email-address return it
        if (param.containsKey("email")) {
            String email = param.get("email")[0];
            if (!EmailValidator.getInstance(false).isValid(email)) {
                throw new UnidentifiedWorkerException(String.format("invalid email: %s!", email));
            }
            return email;
            //if the worker does not provide an email-address return not an email
            // (this can not be found in our database, since our identification is an email.
            // The worker then gets asked for an email).
        } else {
            return "_!";
        }
    }
}
