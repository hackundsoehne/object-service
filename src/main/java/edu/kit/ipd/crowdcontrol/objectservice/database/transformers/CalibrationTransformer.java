package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Calibration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms calibration protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class CalibrationTransformer extends AbstractTransformer {
    /**
     * Converts a calibration record to its protobuf representation.
     *
     * @param record calibration record
     *
     * @return calibration.
     */
    public static Calibration toProto(CalibrationRecord record, List<CalibrationAnswerOptionRecord> options) {
        return Calibration.newBuilder()
                .setId(record.getIdCalibration())
                .setName(record.getName())
                .setQuestion(record.getProperty())
                .addAllAnswers(
                        options.stream()
                                .map(option ->
                                        Calibration.Answer.newBuilder()
                                        .setAnswer(option.getAnswer())
                                        .setId(option.getIdCalibrationAnswerOption())
                                        .build()
                                )
                                .collect(Collectors.toList()))
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
    public static CalibrationRecord mergeRecord(CalibrationRecord target, Calibration calibration) {
        return merge(target, calibration, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Calibration.NAME_FIELD_NUMBER:
                    record.setName(calibration.getName());
                case Calibration.QUESTION_FIELD_NUMBER:
                    record.setProperty(calibration.getQuestion());
                    break;
            }
        });
    }
}
