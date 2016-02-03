package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.InternalServerErrorException;
import spark.Request;

public class RequestUtil {
    private RequestUtil() {
        // utility class
    }

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

    public static boolean getQueryBool(Request request, String param, boolean defaultValue) {
        String paramValue = request.queryParams(param);

        if (paramValue == null) {
            return defaultValue;
        }

        switch(paramValue.toLowerCase()) {
            case "1":
            case "true":
                return true;
            case "0":
            case "false":
                return false;
            default:
                throw new BadRequestException("Query parameter '%s' must be a valid boolean, '%s' given. Use true / 1 / false / 0.", param, paramValue);
        }
    }
}
