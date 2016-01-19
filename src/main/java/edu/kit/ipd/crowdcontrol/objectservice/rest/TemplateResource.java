package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TemplateOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.proto.TemplateList;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.QueryParamUtil.getBoolean;
import static edu.kit.ipd.crowdcontrol.objectservice.rest.QueryParamUtil.getInteger;

/**
 * Handles requests to template resources.
 *
 * @author Niklas Keller
 */
public class TemplateResource {
    private TemplateOperations operations;

    public TemplateResource(TemplateOperations operations) {
        this.operations = operations;
    }

    /**
     * @param request
     *         Request provided by Spark.
     * @param response
     *         Response provided by Spark.
     *
     * @return A list of all templates.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getInteger(request, "from", 0);
        boolean asc = getBoolean(request, "asc", true);

        return operations.all(from, asc, 20)
                .constructPaginated(TemplateList.newBuilder(), TemplateList.Builder::addAllItems)
                .orElseThrow(() -> new NotFoundException("Resources not found."));
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

        return operations.get(id)
                .orElseThrow(() -> new NotFoundException("Resource not found."));
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

        return operations.create(template);
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

        return operations.update(id, template);
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

        boolean existed = operations.delete(id);
        if (!existed) {
            throw new NotFoundException("Template does not exist!");
        }

        return null;
    }
}