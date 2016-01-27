package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.CalibrationOperations;
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
 * Handles requests to calibration resources.
 *
 * @author Niklas Keller
 */
public class CalibrationResource {
    private CalibrationOperations operations;

    public CalibrationResource(CalibrationOperations operations) {
        this.operations = operations;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return a list of all calibrations.
     */
    public Paginated<Integer> all(Request request, Response response) {
        int from = getQueryInt(request, "from", 0);
        boolean asc = getQueryBool(request, "asc", true);

        return operations.getCalibrationsFrom(from, asc, 20)
                .constructPaginated(PopulationList.newBuilder(), PopulationList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return a single calibration.
     */
    public Population get(Request request, Response response) {
        return operations.getCalibration(getParamInt(request, "id"))
                .orElseThrow(() -> new NotFoundException("Resource not found."));
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return the created calibration.
     */
    public Population put(Request request, Response response) {
        Population population = request.attribute("input");

        try {
            population = operations.insertCalibration(population);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Name and content must be set!");
        }

        response.status(201);
        response.header("Location", "/calibrations/" + population.getId());

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
            boolean existed = operations.deleteCalibration(getParamInt(request, "id"));

            if (!existed) {
                throw new NotFoundException("Calibration does not exist!");
            }

            return null;
        } catch (IllegalArgumentException e) {
            throw new ConflictException("Calibration is still in use and can't be deleted.");
        }
    }
}