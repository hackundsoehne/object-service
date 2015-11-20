package edu.ipd.kit.crowdcontrol.proto;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by marcel on 20.11.15.
 */
public class Config {
    private static Properties config;
    public static void init() throws Exception {
        config = new Properties();
        try {
            config.load(new FileInputStream("ccp.properties"));
        } catch (IOException e) {
            throw new Exception("Failed to load config");
        }
    }
    public static String getAccessKeyId() {
        return config.getProperty("AccessKeyId");
    }
    public static String getSecretAccessKey() {
        return config.getProperty("SecretAccessKey");
    }
}
