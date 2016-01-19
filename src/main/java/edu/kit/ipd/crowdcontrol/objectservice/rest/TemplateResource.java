package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TemplateOperation;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.proto.TemplateList;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.api.QueryParamUtil.getBoolean;
import static edu.kit.ipd.crowdcontrol.objectservice.api.QueryParamUtil.getInteger;

/**
 * Handles requests to template resources.
 *
 * @author Niklas Keller
 */
public class TemplateResource {
    private TemplateOperation operation;

    public TemplateResource(TemplateOperation operation) {
        this.operation = operation;
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return A list of all templates.
     */
    public TemplateList all(Request request, Response response) {
        int from = getInteger(request, "from", 1);
        boolean asc = getBoolean(request, "asc", true);

        TemplateList list = operation.all(from, asc);

        if (list.getItemsCount() == 0) {
            throw new NotFoundException("Resources not found.");
        }

        return list;
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
            id = Integer.parseInt(request.params("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID must be a valid integer.");
        }

        Template template = operation.get(id);

        if (template == null) {
            throw new NotFoundException("Resource not found.");
        }

        return template;
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
        Template template = request.attribute("input");

        return operation.create(template);
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
        int id;

        try {
            id = Integer.parseInt(request.params("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID must be a valid integer.");
        }

        Template template = request.attribute("input");

        return operation.update(id, template);
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
        int id;

        try {
            id = Integer.parseInt(request.params("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID must be a valid integer.");
        }

        if (!operation.delete(id)) {
            throw new NotFoundException("There's no template with ID '%d'", id);
        }

        return null;
    }
}