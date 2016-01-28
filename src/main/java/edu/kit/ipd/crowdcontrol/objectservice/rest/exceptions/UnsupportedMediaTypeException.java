package edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions;

import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;

/**
 * Thrown on requests with an invalid content-type header.
 *
 * @author Niklas Keller
 */
public class UnsupportedMediaTypeException extends BadRequestException {
    private final String[] supportedTypes;

    /**
     * @param providedType   Sent content-type value.
     * @param supportedTypes Accepted content-type values.
     */
    public UnsupportedMediaTypeException(String providedType, String... supportedTypes) {
        super("Provided content-type is not supported: '%s'.", providedType);
        this.supportedTypes = supportedTypes;
    }

    /**
     * @return Supported content-type values.
     */
    public String[] getSupportedTypes() {
        return supportedTypes;
    }
}
