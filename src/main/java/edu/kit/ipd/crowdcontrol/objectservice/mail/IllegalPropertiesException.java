package edu.kit.ipd.crowdcontrol.objectservice.mail;

/**
 * This exception gets thrown, if a properties file in a mail handler is not valid.
 * @author felix
 */
public class IllegalPropertiesException extends Exception {

    public IllegalPropertiesException() {
        super("The properties object is not valid for the certain purpose");
    }
}

