package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.Range;
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
    private TemplateOperations operation;

    public TemplateResource(TemplateOperations operation) {
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
    public Paginated<Integer> all(Request request, Response response) {
        int from = getInteger(request, "from", 0);
        boolean asc = getBoolean(request, "asc", true);

        Range<Template, Integer> range = operation.all(from, asc, 20);

        if (range.getData().isEmpty()) {
            throw new NotFoundException("Resources not found.");
        }

        TemplateList.Builder builder = TemplateList.newBuilder();
        range.getData().forEach(builder::addItems);

        return new Paginated<>(builder.build(), range.getLeft(), range.getRight(), range.hasPredecessors(), range.hasSuccessors());
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

        operation.delete(id);

        return null;
    }
}