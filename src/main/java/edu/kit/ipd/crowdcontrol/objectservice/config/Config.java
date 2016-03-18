package edu.kit.ipd.crowdcontrol.objectservice.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Config of which is saved in config file.
 *
 * @author Marcel Hollerbach
 * @author Niklas Keller
 */
public class Config {
    /**
     * Database configuration
     */
    public Database database = new Database();

    /**
     * Deployment configuration
     */
    public Deployment deployment = new Deployment();

    /**
     * Platform configurations
     */
    public ConfigPlatform[] platforms;

    /**
     * Money transfer configuration
     */
    public MoneyTransfer moneytransfer = new MoneyTransfer();

    /**
     * Mail configuration
     */
    public Mail mail = new Mail();

    /**
     * Log level configuration
     *
     * Maps package to log constant, e.g. 'org.eclipse.jetty: WARN'.
     */
    public Map<String, String> log = new HashMap<>();

    /**
     * the jwtsecret for the jwt-token.
     */
    public String jwtsecret;
}
