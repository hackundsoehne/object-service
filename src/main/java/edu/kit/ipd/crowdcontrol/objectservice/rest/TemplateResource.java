package edu.kit.ipd.crowdcontrol.objectservice.rest;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TemplateOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.proto.TemplateList;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

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
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

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
        return operations.get(getParamInt(request, "id"))
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
        Template template = request.attribute("input");
        return operations.update(getParamInt(request, "id"), template);
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
        boolean existed = operations.delete(getParamInt(request, "id"));

        if (!existed) {
            throw new NotFoundException("Template does not exist!");
        }

        return null;
    }
}