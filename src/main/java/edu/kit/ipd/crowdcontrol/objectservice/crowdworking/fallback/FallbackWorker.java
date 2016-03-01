package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.fallback;

import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.UnidentifiedWorkerException;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentification;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.WorkerIdentificationComputation;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The fallback-worker uses the email-address to identify workers.
 * @author LeanderK
 * @version 1.0
 */
public class FallbackWorker implements Function<String, WorkerIdentificationComputation> {

    /**
     * Applies this function to the given argument.
     *
     * @param platform the name of the platform
     * @return the function result
     */
    @Override
    public WorkerIdentificationComputation apply(String platform) {
        return params -> {
            //if the worker does not provide an email-address return not an email
            // (this can not be found in our database, since our identification is an email.
            // The worker then gets asked for an email).
            String data = "_!";
            //if the worker provides an email-address return it
            if (params.containsKey("email")) {
                String email = params.get("email")[0];
                if (!email.isEmpty() && !EmailValidator.getInstance(false).isValid(email)) {
                    throw new UnidentifiedWorkerException(String.format("invalid email: %s!", email));
                }
                data = email;
            }
            return WorkerIdentification.findByIdentification(platform, data);
        };
    }
}
