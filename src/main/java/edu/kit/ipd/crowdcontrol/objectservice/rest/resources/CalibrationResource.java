package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.CalibrationOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import edu.kit.ipd.crowdcontrol.objectservice.proto.CalibrationList;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.ConflictException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import spark.Request;
import spark.Response;

import java.util.Optional;

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
                .constructPaginated(CalibrationList.newBuilder(), CalibrationList.Builder::addAllItems);
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return a single calibration.
     */
    public Calibration get(Request request, Response response) {
        return operations.getCalibration(getParamInt(request, "id"))
                .orElseThrow(() -> new NotFoundException("Resource not found."));
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return the created calibration.
     */
    public Calibration put(Request request, Response response) {
        Calibration calibration = request.attribute("input");

        if (calibration.getAnswersList().isEmpty())
            throw new BadRequestException("Calibrations must have answers!");

        try {
            calibration = operations.insertCalibration(calibration);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Missing at least one required parameter.");
        }

        EventManager.CALIBRATION_CREATE.emit(calibration);

        response.status(201);
        response.header("Location", "/calibrations/" + calibration.getId());

        return calibration;
    }

    /**
     * @param request  request provided by Spark
     * @param response response provided by Spark
     *
     * @return {@code null}.
     */
    public Calibration delete(Request request, Response response) {
        int id = getParamInt(request, "id");

        try {
            Optional<Calibration> calibration = operations.getCalibration(id);
            calibration.map(EventManager.CALIBRATION_DELETE::emit);

            boolean existed = operations.deleteCalibration(id);

            if (!existed) {
                throw new NotFoundException("Calibration does not exist!");
            }

            return null;
        } catch (IllegalArgumentException e) {
            throw new ConflictException("Calibration is still in use and can't be deleted.");
        }
    }
}