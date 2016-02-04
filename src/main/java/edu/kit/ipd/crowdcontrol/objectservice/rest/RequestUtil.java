package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import spark.Request;

/**
 * Utility class to simplify access to request variables. Static methods can be used in the resource
 * classes handling the HTTP requests.
 *
 * @author Niklas Keller
 */
public class RequestUtil {
    private RequestUtil() {
        // utility class
    }

    /**
     * Parses a parameter from the URL to an integer.
     *
     * @param request request provided by Spark
     * @param param   parameter name
     *
     * @return Parameter value as an integer.
     *
     * @throws InternalServerErrorException If the specified parameter was not contained in the URL.
     *                                      This is always an implementation error.
     * @throws BadRequestException          If the parameter can't be converted to an integer.
     */
    public static int getParamInt(Request request, String param) {
        String paramValue = request.params(param);

        if (paramValue == null) {
            throw new InternalServerErrorException("Expected parameter '%s' not present.", param);
        }

        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Parameter '%s' must be a valid integer, '%s' given.", param, paramValue);
        }
    }

    /**
     * Parses a query string parameter to an integer or uses the default value if the parameter is
     * not provided.
     *
     * @param request      request provided by Spark
     * @param param        query parameter name
     * @param defaultValue default value to use when parameter is not present
     *
     * @return Parsed integer or default value.
     *
     * @throws BadRequestException If parameter is present but not a valid integer.
     */
    public static int getQueryInt(Request request, String param, int defaultValue) {
        String paramValue = request.queryParams(param);

        if (paramValue == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Query parameter '%s' must be a valid integer, '%s' given.", param, paramValue);
        }
    }

    /**
     * Parses a query string parameter to a boolean or uses the default value if the parameter is
     * not provided.
     *
     * @param request      request provided by Spark
     * @param param        query parameter name
     * @param defaultValue default value to use when parameter is not present
     *
     * @return Parsed boolean or default value.
     *
     * @throws BadRequestException If parameter is present but not a valid boolean.
     */
    public static boolean getQueryBool(Request request, String param, boolean defaultValue) {
        String paramValue = request.queryParams(param);

        if (paramValue == null) {
            return defaultValue;
        }

        switch (paramValue.toLowerCase()) {
            case "1":
                return true;
            case "true":
                return true;
            case "0":
                return false;
            case "false":
                return false;
            default:
                throw new BadRequestException("Query parameter '%s' must be a valid boolean, '%s' given. Use true / 1 / false / 0.", param, paramValue);
        }
    }
}
