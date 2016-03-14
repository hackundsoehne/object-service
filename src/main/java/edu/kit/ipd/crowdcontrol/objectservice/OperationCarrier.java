package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.config.Config;
import edu.kit.ipd.crowdcontrol.objectservice.database.DatabaseManager;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.*;
import org.jooq.DSLContext;

import java.sql.SQLException;

/**
 * A class which carries all Database Operations
 *
 * @author Marcel Hollerbach
 */
class OperationCarrier {

    public final TemplateOperations templateOperations;
    public final NotificationOperations notificationRestOperations;
    public final PlatformOperations platformOperations;
    public final WorkerOperations workerOperations;
    public final CalibrationOperations calibrationOperations;
    public final ExperimentOperations experimentOperations;
    public final TagConstraintsOperations tagConstraintsOperations;
    public final AlgorithmOperations algorithmsOperations;
    public final WorkerCalibrationOperations workerCalibrationOperations;
    public final AnswerRatingOperations answerRatingOperations;
    public final ExperimentsPlatformOperations experimentsPlatformOperations;
    public final WorkerBalanceOperations workerBalanceOperations;

    /**
     * Create this object and init all operations for the given Database manager
     * @param config config to use for
     * @param manager manager to connect to
     * @throws SQLException for the case there is a problem with SQL
     */
    public OperationCarrier(Config config, DatabaseManager manager) throws SQLException {
        DSLContext ctx = manager.getContext();
        templateOperations = new TemplateOperations(ctx);
        notificationRestOperations = new NotificationOperations(manager, config.database.readonly.user, config.database.readonly.password);
        platformOperations = new PlatformOperations(ctx);
        workerOperations = new WorkerOperations(ctx);
        calibrationOperations = new CalibrationOperations(ctx);
        experimentOperations = new ExperimentOperations(ctx);
        tagConstraintsOperations = new TagConstraintsOperations(ctx);
        algorithmsOperations = new AlgorithmOperations(ctx);
        workerCalibrationOperations = new WorkerCalibrationOperations(ctx);
        answerRatingOperations = new AnswerRatingOperations(ctx, calibrationOperations, workerCalibrationOperations, experimentOperations);
        experimentsPlatformOperations = new ExperimentsPlatformOperations(ctx);
        workerBalanceOperations = new WorkerBalanceOperations(ctx);
    }
}
