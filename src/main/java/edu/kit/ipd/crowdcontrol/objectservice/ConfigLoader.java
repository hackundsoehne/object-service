package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigException;
import edu.kit.ipd.crowdcontrol.objectservice.config.ConfigPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.SQLDialect;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

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

        Yaml yaml = new Yaml(new Constructor(Config.class));
        config = (Config) yaml.load(configStream);
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


        configValidate(config);

        config.database.url = appendUtf8Settings(config.database.url);
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

    private void configPlatformTypeValidate(ConfigPlatform platform, boolean apiKey, boolean projectId, boolean user, boolean password) throws ConfigException {
        if (!apiKey && platform.apiKey != null)
            throw new ConfigException("ApiKey is not used at "+platform.type);
        if (apiKey && platform.apiKey == null)
            throw new ConfigException("ApiKey is needed at "+platform.type);

        if (!projectId && platform.projectId != null)
            throw new ConfigException("ProjectId is not used at "+platform.type);
        if (projectId && platform.projectId == null)
            throw new ConfigException("ProjectId is needed at "+platform.type);

        if (!user && platform.user != null)
            throw new ConfigException("User is not used at "+platform.type);
        if (user && platform.user == null)
            throw new ConfigException("User is needed at "+platform.type);

        if (!password && platform.password != null)
            throw new ConfigException("Password is  not used at "+platform.type);
        if (password && platform.password == null)
            throw new ConfigException("Password is needed at "+platform.type);
    }

    private void configPlatformsValidate() throws ConfigException {
        for (int i = 0; i < config.platforms.length; i++) {
            ConfigPlatform platform = config.platforms[i];

            switch (platform.type.toLowerCase()) {
                case "mturk":
                    configPlatformTypeValidate(platform, false, false, true, true);
                    break;
                case "pybossa":
                    configPlatformTypeValidate(platform, true, true, false, false);
                    break;
                case "dummy":
                    configPlatformTypeValidate(platform, false, false, false, false);
                    break;
                case "local":
                    configPlatformTypeValidate(platform, false, false, false, false);
                    break;
                default:
                    throw new ConfigException("Type "+platform.type+" is not found");
            }

            if (platform.name == null)
                throw new ConfigException("platform name must be set for platforms!  (At platform with type: "+platform.type+ ")");

            if (platform.url == null)
                throw new ConfigException("a url must be set for platforms! (At platform with name: "+platform.name+ ")");

            if (Boolean.getBoolean(platform.name.toLowerCase() + ".disabled")) {
                config.platforms[i] = null;
            }
        }

        config.platforms = Arrays.stream(config.platforms)
                .filter(configPlatform -> configPlatform != null)
                .toArray(ConfigPlatform[]::new);
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
            throw new ConfigException("database.writing and database.readonly or database.databasepool have to be set correctly");
        if (NullOrEmpty(config.database.url))
            throw new ConfigException("database.url is not present!");
        if (NullOrEmpty(config.deployment.jwtsecret))
            throw new ConfigException("deployment.jwtsecret not set");
        if (NullOrEmpty(config.deployment.workerService))
            throw new ConfigException("deployment.workerService is not found");
        if (NullOrEmpty(config.deployment.workerUILocal) && config.deployment.workerUIPublic == null)
            throw new ConfigException("deployment.workerUILocal urls are not found!");
        if (NullOrEmpty(config.moneytransfer.notificationMailAddress))
            throw new ConfigException("moneytransfer.notificationMailAddress mail adress is empty");
        configPlatformsValidate();
    }

    public Config getConfig() {
        return config;
    }
}
