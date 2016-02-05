package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * A Platform configured in the config
 */
public class ConfigPlatform {
    /**
     * type of platform to use
     */
    public String type;
    /**
     * user which gets passed to the platform
     */
    public String user;
    /**
     * password which gets passed to the platform
     */
    public String password;
    /**
     * url to use for the database
     */
    public String url;
    /**
     * how exactly the platform is named in the db
     */
    public String name;
}
