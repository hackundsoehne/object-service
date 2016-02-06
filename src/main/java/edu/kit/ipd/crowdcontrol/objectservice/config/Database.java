package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * Representation of the database section in the config
 */
public class Database {
    /**
     * how many hours have to went by before the database maintains itself (cleanup etc.)
     */
    public int maintainInterval;

    /**
     * URL of the db to use
     */
    public String url;

    /**
     * The dialekt which is used by the id
     */
    public String dialect;

    /**
     * 2 users, one MUST be able to write, the only should only have ro rights, this is used to query notification triggers
     */
    public Credentials writing, readonly;

    /**
     * A possible databasepool name to use to login
     */
    public String databasepool;
}
