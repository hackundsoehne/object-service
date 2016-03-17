package edu.kit.ipd.crowdcontrol.objectservice;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import edu.kit.ipd.crowdcontrol.objectservice.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utilsclass for common operations
 *
 * @author Marcel Hollerbach
 */
public class Utils {
    private static final Logger LOGGER = LogManager.getLogger("Utils");
    /**
     * Loads a file located relative to the main class
     * @param file the filename to load
     * @return the content of the file
     */
    public static String loadFile(String file) {
        try {
            return CharStreams
                    .toString(new InputStreamReader(
                            Main.class.getResourceAsStream(file)
                            , Charsets.UTF_8));
        } catch (IOException e) {
            LOGGER.fatal("Loading file "+file+" failed", e);
            return "";
        }
    }
}
