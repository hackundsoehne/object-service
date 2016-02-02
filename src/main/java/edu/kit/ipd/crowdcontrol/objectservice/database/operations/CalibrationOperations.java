package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.CalibrationAnswerOption;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsCalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.CalibrationTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * @author Niklas Keller
 * @author LeanderK
 */
public class CalibrationOperations extends AbstractOperations {
    /**
     * Creates a new calibration operations instance.
     *
     * @param create context used to communicate with the database
     */
    public CalibrationOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of calibrations starting from {@code cursor}.
     *
     * @param cursor Pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     * @return List of calibrations
     */
    public Range<Calibration, Integer> getCalibrationsFrom(int cursor, boolean next, int limit) {
        // Join is more complicated and the performance gain would be negligible considering the the
        // expected moderate usage
        SelectConditionStep<CalibrationRecord> query = create.selectFrom(CALIBRATION).where(CALIBRATION.EXPERIMENT.isNull());
        return getNextRange(query, CALIBRATION.ID_CALIBRATION, CALIBRATION, cursor, next, limit)
                .map(calibrationRecord -> {
                    List<CalibrationAnswerOptionRecord> answers = create.selectFrom(CALIBRATION_ANSWER_OPTION)
                            .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibrationRecord.getIdCalibration()))
                            .fetch();

                    return CalibrationTransformer.toProto(calibrationRecord, answers);
                });
    }

    /**
     * Returns a single calibration.
     *
     * @param id ID of the calibration
     *
     * @return The calibration
     */
    public Optional<Calibration> getCalibration(int id) {
        // Join is more complicated and the performance gain would be negligible considering the the
        // expected moderate usage
        return create.fetchOptional(CALIBRATION, Tables.CALIBRATION.ID_CALIBRATION.eq(id))
                .map(calibrationRecord -> {
                    List<CalibrationAnswerOptionRecord> answers = create.selectFrom(CALIBRATION_ANSWER_OPTION)
                            .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibrationRecord.getIdCalibration()))
                            .fetch();

                    return CalibrationTransformer.toProto(calibrationRecord, answers);
                });
    }

    /**
     * Creates a new calibration.
     *
     * @param toStore calibration to save
     *
     * @return Calibration with ID assigned
     *
     * @throws IllegalArgumentException if the name or content is not set
     */
    public Calibration insertCalibration(Calibration toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Calibration.NAME_FIELD_NUMBER,
                Calibration.QUESTION_FIELD_NUMBER,
                Calibration.ANSWERS_FIELD_NUMBER);

        CalibrationRecord calibration = CalibrationTransformer.mergeRecord(create.newRecord(CALIBRATION), toStore);
        calibration.store();

        toStore.getAnswersList().stream()
                .map(s -> new CalibrationAnswerOptionRecord(null, calibration.getIdCalibration(), s.getAnswer()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), create::batchInsert))
                .execute();

        List<CalibrationAnswerOptionRecord> answers = create.selectFrom(CALIBRATION_ANSWER_OPTION)
                .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibration.getIdCalibration()))
                .fetch();

        return CalibrationTransformer.toProto(calibration, answers);
    }

    /**
     * Deletes a calibration.
     *
     * @param id ID of the calibration
     *
     * @return {@code true} if deleted, {@code false} otherwise
     * @throws IllegalArgumentException if the calibration is still in use
     */
    public boolean deleteCalibration(int id) throws IllegalArgumentException{
        boolean isUsed = create.fetchExists(
                DSL.select()
                    .from(EXPERIMENTS_CALIBRATION)
                    .join(CALIBRATION_ANSWER_OPTION).onKey()
                    .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(id))
        );

        if (isUsed) {
            throw new IllegalArgumentException(String.format("Calibration %d is still in used", id));
        }

        return create.deleteFrom(CALIBRATION)
                .where(CALIBRATION.ID_CALIBRATION.eq(id))
                .execute() == 1;
    }

    /**
     * stores the chosen calibrations for the experiment in the database
     * @param platform the platform chosen
     * @param calibrationIDs the primary key of the CalibrationAnswerOptions
     * @param experimentID the primary key of the experiment
     */
    public void storeExperimentCalibrations(String platform, List<Integer> calibrationIDs, int experimentID) {
        List<ExperimentsCalibrationRecord> calibrations = calibrationIDs.stream()
                .map(calibration -> new ExperimentsCalibrationRecord(null, experimentID, calibration, platform, false))
                .collect(Collectors.toList());
        create.transaction(conf -> {
            DSL.using(conf).deleteFrom(EXPERIMENTS_CALIBRATION)
                    .where(EXPERIMENTS_CALIBRATION.REFERNCED_EXPERIMENT.eq(experimentID))
                    .execute();
            DSL.using(conf).batchInsert(calibrations).execute();
        });
    }

    /**
     * Returns a CalibrationAnswerOptionRecord with the given id
     * @param id the id
     * @return a value if one can be found or empty if not
     */
    public Optional<CalibrationAnswerOptionRecord> getCalibrationAnswerOption(int id) {
        return create.fetchOptional(CALIBRATION_ANSWER_OPTION,
                CALIBRATION_ANSWER_OPTION.ID_CALIBRATION_ANSWER_OPTION.eq(id));
    }

    /**
     * gets the CalibrationAnswerOption associated with the experiment
     * @param experimentID the id of the experiment
     * @return the record if found
     */
    public Optional<CalibrationAnswerOptionRecord> getCalibrationForExperiment(int experimentID) {
        return create.selectFrom(CALIBRATION_ANSWER_OPTION)
                .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(
                        DSL.select(CALIBRATION.ID_CALIBRATION)
                                .from(CALIBRATION)
                                .where(CALIBRATION.EXPERIMENT.eq(experimentID))
                ))
                .fetchOptional();
    }

    /**
     * create the internal Calibration used to connect worker to experiments.
     * @param experimentId the experiment
     */
    public void createExperimentsCalibration(int experimentId) {
        String description = "experiment: " + experimentId;
        CalibrationRecord record = new CalibrationRecord(null, description, description, experimentId);
        CalibrationAnswerOptionRecord answer = new CalibrationAnswerOptionRecord(null, null, description);
        create.transactionResult(conf -> {
                    boolean exists = DSL.using(conf).fetchExists(
                            DSL.selectFrom(CALIBRATION)
                                    .where(CALIBRATION.EXPERIMENT.eq(experimentId))
                    );
                    if (!exists) {
                        return create.insertInto(CALIBRATION)
                                .set(record)
                                .returning()
                                .fetchOptional();
                    }
                    return Optional.<CalibrationRecord>empty();
                })
                .map(CalibrationRecord::getIdCalibration)
                .ifPresent(id -> create.transaction(conf -> {
                    boolean exists = DSL.using(conf).fetchExists(
                            DSL.selectFrom(CALIBRATION_ANSWER_OPTION)
                                    .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(id))
                    );
                    if (!exists) {
                        create.insertInto(CALIBRATION_ANSWER_OPTION)
                                .set(answer)
                                .execute();
                    }
                }));
    }
}
