package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * Called if there is a error in the config
 * @author MarcelHollerbach
 * @version 1.0
 */
public class ConfigException extends Exception {
    /**
     * Creates a new exception
     * @param s message where everything failed
     */
    public ConfigException(String s) {
        super(s);
    }
}
