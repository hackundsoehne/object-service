package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PopulationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Population;
import edu.kit.ipd.crowdcontrol.objectservice.proto.PopulationList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.ConflictException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import static edu.kit.ipd.crowdcontrol.objectservice.rest.RequestUtil.*;

/**
 * Handles requests to population resources.
 *
 * @author Niklas Keller
 */
public class PopulationResource {
    private PopulationOperations operations;

    public PopulationResource(PopulationOperations operations) {
        this.operations = operations;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return List of populations.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return operations.getPopulationFrom(from, asc, 20)
                .constructPaginated(PopulationList.newBuilder(), PopulationList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Single population.
     */
    public Population get(Request request, Response response) {
        return operations.getPopulation(getParamInt(request, "id"))
                .orElseThrow(NotFoundException::new);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return Created population.
     */
    public Population put(Request request, Response response) {
        Population population = request.attribute("input");

        try {
            population = operations.insertPopulation(population);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Missing at least one required parameter.");
        }

        response.status(201);
        response.header("Location", "/populations/" + population.getId());

        return population;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return {@code null}.
     */
    public Population delete(Request request, Response response) {
        try {
            boolean existed = operations.deletePopulation(getParamInt(request, "id"));

            if (!existed) {
                throw new NotFoundException();
            }

            return null;
        } catch (IllegalArgumentException e) {
            throw new ConflictException("Population is still in use and can't be deleted.");
        }
    }
}