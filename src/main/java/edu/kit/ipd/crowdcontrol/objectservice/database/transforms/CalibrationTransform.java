package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.calibration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms calibration protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class CalibrationTransform extends AbstractTransform {
    /**
     * Converts a calibration record to its protobuf representation.
     *
     * @param record calibration record
     *
     * @return calibration.
     */
    public static calibration toProto(CalibrationRecord record, List<CalibrationAnswerOptionRecord> options) {
        return calibration.newBuilder()
                .setId(record.getIdCalibration())
                .setName(record.getName())
                .setQuestion(record.getProperty())
                .addAllAnswers(options.stream().map(CalibrationAnswerOptionRecord::getAnswer).collect(Collectors.toList()))
                .build();
    }

    /**
     * Merges a record with the set properties of a protobuf calibration.
     *
     * @param target record to merge into
     * @param calibration message to merge from
     *
     * @return Merged calibration record.
     */
    public static CalibrationRecord mergeRecord(CalibrationRecord target, calibration calibration) {
        return merge(target, calibration, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case edu.kit.ipd.crowdcontrol.objectservice.proto.calibration.NAME_FIELD_NUMBER:
                    record.setName(calibration.getName());
                case edu.kit.ipd.crowdcontrol.objectservice.proto.calibration.QUESTION_FIELD_NUMBER:
                    record.setProperty(calibration.getQuestion());
                    break;
            }
        });
    }
}
