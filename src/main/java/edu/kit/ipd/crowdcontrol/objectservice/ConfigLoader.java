package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ho.yaml.Yaml;
import org.jooq.SQLDialect;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the config from desired places and validates the completeness of the config
 *
 * @author Marcel Hollerbach
 */
public class ConfigLoader {
    private static final Logger LOGGER = LogManager.getLogger("Config");
    private final Config config;

    /**
     * Creates a new loader object
     * @throws FileNotFoundException For the case no config is found
     * @throws ConfigException For the case the config is not valid
     */
    public ConfigLoader() throws FileNotFoundException, ConfigException {
        InputStream configStream;
        if (System.getProperty("objectservice.config") != null) {
            LOGGER.debug("loading configuration from location: {}", System.getProperty("objectservice.config"));
            configStream = new FileInputStream(System.getProperty("objectservice.config"));
        } else {
            configStream = Main.class.getResourceAsStream("/config.yml");
        }

        config = Yaml.loadType(configStream, Config.class);
        if (System.getProperty("workerservice.url") != null) {
            config.deployment.workerService = System.getProperty("workerservice.url");
        }
        if (System.getProperty("origin.url") != null) {
            config.deployment.origin = System.getProperty("origin.url");
        }
        if (System.getProperty("workeruipublic.url") != null) {
            config.deployment.workerUIPublic = System.getProperty("workeruipublic.url");
        }
        if (System.getProperty("workeruilocal.url") != null) {
            config.deployment.workerUILocal = System.getProperty("workeruilocal.url");
        }
        if (System.getProperty("jwt.secret") != null) {
            config.deployment.workerUILocal = System.getProperty("jwt.secret");
        }


        try {
            config.platforms = Arrays.stream(config.platforms)
                    .map(platform -> {
                        if (platform.name == null) {
                            if (platform.type.equals("dummy")) {
                                platform.name = "Dummy";
                            } else {
                                throw new RuntimeException(new ConfigException("Platform (type: " + platform.type + ") without a name will be ignored."));
                            }
                        }

                        return platform;
                    })
                    .filter(platform -> !Boolean.getBoolean(platform.name.toLowerCase() + ".disabled"))
                    .toArray(ConfigPlatform[]::new);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConfigException) {
                throw (ConfigException) e.getCause();
            } else {
                throw e;
            }
        }

        if (config.database.url != null) {
            config.database.url = appendUtf8Settings(config.database.url);
        }

        configValidate(config);
    }

    private String appendUtf8Settings(String url) {
        String[] parts = url.split("\\?", 2);

        String base = parts[0];
        String query = "";

        if (parts.length == 2) {
            query = parts[1];
        }

        Map<String, String> queryParams = new HashMap<>();

        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=");
            queryParams.put(keyValue[0], keyValue.length == 2 ? keyValue[1] : null);
        }

        queryParams.put("useUnicode", "true");
        queryParams.put("characterEncoding", "UTF-8");

        query = "";

        for (String key : queryParams.keySet()) {
            query += key;

            String value = queryParams.get(key);

            if (value != null) {
                query += "=" + value;
            }

            query += "&";
        }

        if (!query.isEmpty()) {
            query = query.substring(0, query.length() - 1);
        }

        return base + "?" + query;
    }

    /**
     * check if a string is empty or null
     * @param val the value to check
     * @return true if the value is not null and not empty
     */
    private boolean NullOrEmpty(String val) {
        return !(val != null && !val.isEmpty());
    }

    /**
     * Validate the given config
     * @param config config to validate
     * @throws ConfigException thrown when there is a config exception
     */
    private void configValidate(Config config) throws ConfigException {
        if (config.database.maintainInterval < 0)
            throw new ConfigException("negative maintainInterval of database is not valid");
        if (NullOrEmpty(config.database.dialect) || SQLDialect.valueOf(config.database.dialect) == null)
            throw new ConfigException("Dialect does not exist");
        if ((config.database.writing == null ||
                config.database.readonly == null) &&
                NullOrEmpty(config.database.databasepool))
            throw new ConfigException("Database users have to be set!");
        if (NullOrEmpty(config.database.url))
            throw new ConfigException("Database url is not present!");
        if (NullOrEmpty(config.deployment.workerService))
            throw new ConfigException("WorkerService is not found");
        if (NullOrEmpty(config.deployment.workerUILocal) && config.deployment.workerUIPublic == null)
            throw new ConfigException("WorkerUi urls are not found!");
        if (NullOrEmpty(config.moneytransfer.notificationMailAddress))
            throw new ConfigException("Notification mail adress is empty");
        if (NullOrEmpty(config.jwtsecret)) {
            throw new ConfigException("JWT-jwtsecret is not set");
        }
    }

    public Config getConfig() {
        return config;
    }
}
