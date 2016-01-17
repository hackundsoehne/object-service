package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.proto.TemplateList;
import spark.Request;
import spark.Response;

/**
 * Handles requests to template resources.
 *
 * @author Niklas Keller
 */
public class TemplateResource {
    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return A list of all templates.
     */
    public TemplateList all(Request request, Response response) {
        return TemplateList.newBuilder()
                .addItems(Template.newBuilder().setId(1).setContent("{{TEST}}").build())
                .addItems(Template.newBuilder().setId(2).setContent("{{TEST}}").build())
                .addItems(Template.newBuilder().setId(3).setContent("{{TEST}}").build())
                .build();
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return A single template.
     */
    public Template get(Request request, Response response) {
        int id;

        try {
            id = Integer.parseInt(request.params(":id"));
        } catch (NumberFormatException e) {
            throw new BadRequestException(":id must be a valid integer.");
        }

        return Template.newBuilder().setId(id).setContent("{{TEST}}").build();
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return The created template.
     */
    public Template put(Request request, Response response) {
        return request.attribute("input");
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return The modified template.
     */
    public Template patch(Request request, Response response) {
        Template template = request.attribute("input");
        template = template.toBuilder().setId(12).build();
        return template;
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return {@code null}.
     */
    public Template delete(Request request, Response response) {
        return null;
    }
}