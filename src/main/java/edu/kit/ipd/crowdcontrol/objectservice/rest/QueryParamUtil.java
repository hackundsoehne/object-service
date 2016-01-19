package edu.kit.ipd.crowdcontrol.objectservice.rest;

import spark.Request;

public class QueryParamUtil {
    public static int getInteger(Request request, String param, int defaultValue) {
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

    public static boolean getBoolean(Request request, String param, boolean defaultValue) {
        String paramValue = request.queryParams(param);

        if (paramValue == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(paramValue);
    }
}
