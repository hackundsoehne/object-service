package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * Created by marcel on 05.02.16.
 */
public class Database {
    public int maintainInterval;
    public String url;
    public String dialect;
    public Credentials writing, readonly;
    public String databasepool;
}
