package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentsCalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.CalibrationTransform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.calibration;
import org.jooq.DSLContext;
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
    public Range<calibration, Integer> getCalibrationsFrom(int cursor, boolean next, int limit) {
        // Join is more complicated and the performance gain would be negligible considering the the
        // expected moderate usage
        return getNextRange(create.selectFrom(CALIBRATION), CALIBRATION.ID_CALIBRATION, cursor, next, limit)
                .map(calibrationRecord -> {
                    List<CalibrationAnswerOptionRecord> answers = create.selectFrom(CALIBRATION_ANSWER_OPTION)
                            .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibrationRecord.getIdCalibration()))
                            .fetch();

                    return CalibrationTransform.toProto(calibrationRecord, answers);
                });
    }

    /**
     * Returns a single calibration.
     *
     * @param id ID of the calibration
     *
     * @return The calibration
     */
    public Optional<calibration> getCalibration(int id) {
        // Join is more complicated and the performance gain would be negligible considering the the
        // expected moderate usage
        return create.fetchOptional(CALIBRATION, Tables.CALIBRATION.ID_CALIBRATION.eq(id))
                .map(calibrationRecord -> {
                    List<CalibrationAnswerOptionRecord> answers = create.selectFrom(CALIBRATION_ANSWER_OPTION)
                            .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibrationRecord.getIdCalibration()))
                            .fetch();

                    return CalibrationTransform.toProto(calibrationRecord, answers);
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
    public calibration insertCalibration(calibration toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                calibration.NAME_FIELD_NUMBER,
                calibration.QUESTION_FIELD_NUMBER,
                calibration.ANSWERS_FIELD_NUMBER);

        CalibrationRecord calibration = CalibrationTransform.mergeRecord(create.newRecord(CALIBRATION), toStore);
        calibration.store();

        toStore.getAnswersList().stream()
                .map(s -> new CalibrationAnswerOptionRecord(null, calibration.getIdCalibration(), s))
                .collect(Collectors.collectingAndThen(Collectors.toList(), create::batchInsert))
                .execute();

        List<CalibrationAnswerOptionRecord> answers = create.selectFrom(CALIBRATION_ANSWER_OPTION)
                .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibration.getIdCalibration()))
                .fetch();

        return CalibrationTransform.toProto(calibration, answers);
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
     * Delete all experiment calibrations
     * @param id the primary key of the experiment
     */
    public void deleteAllExperimentCalibration(int id) {
        create.deleteFrom(EXPERIMENTS_CALIBRATION)
                .where(EXPERIMENTS_CALIBRATION.REFERNCED_EXPERIMENT.eq(id))
                .execute();
    }

    /**
     * Insert a new experimentsCalibrationRecord in the database
     * @param experimentsCalibrationRecord the record to use
     * @return The new inserted record
     */
    public ExperimentsCalibrationRecord insertExperimentCalibration(ExperimentsCalibrationRecord experimentsCalibrationRecord) {
        return create.insertInto(EXPERIMENTS_CALIBRATION)
                .set(experimentsCalibrationRecord)
                .returning()
                .fetchOne();
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
     * Get a AnswerOption from a calibration with the given answer
     * @param calibration The calibration this is a answer from
     * @param answer The answer which should be found
     * @return A record if one is found
     */
    public Optional<CalibrationAnswerOptionRecord> getCalibrationAnswerOptionFromCalibrations(int calibration, String answer) {
        return create.selectFrom(CALIBRATION_ANSWER_OPTION)
                .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(calibration))
                .and(CALIBRATION_ANSWER_OPTION.ANSWER.eq(answer))
                .fetchOptional();
    }
}
