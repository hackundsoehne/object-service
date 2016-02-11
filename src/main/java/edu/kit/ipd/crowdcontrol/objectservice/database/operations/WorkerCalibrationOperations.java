package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.CalibrationResultRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.CalibrationAnswer;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * Responsible for the operations involving the worker calibrations.
 *
 * @author Niklas Keller
 */
public class WorkerCalibrationOperations extends AbstractOperations {
    public WorkerCalibrationOperations(DSLContext create) {
        super(create);
    }

    /**
     * Inserts a new calibration answer.
     * @param workerId ID of the worker
     * @param answerOption the chosen answerOption
     * @return Inserted calibration answer
     * @throws IllegalArgumentException if the worker has already chosen an answer
     */
    public CalibrationAnswer insertAnswer(int workerId, int answerOption) throws IllegalArgumentException {
        Select<Record1<Integer>> possibleAnswers = DSL
                .select(CALIBRATION_ANSWER_OPTION.ID_CALIBRATION_ANSWER_OPTION)
                .from(CALIBRATION_ANSWER_OPTION)
                .where(CALIBRATION_ANSWER_OPTION.CALIBRATION.eq(
                        DSL.select(CALIBRATION_ANSWER_OPTION.CALIBRATION)
                                .where(CALIBRATION_ANSWER_OPTION.ID_CALIBRATION_ANSWER_OPTION.eq(answerOption))
                ));
        Boolean answered = create.transactionResult(conf -> {
            boolean alreadyAnswered = create.fetchExists(
                    DSL.selectFrom(CALIBRATION_RESULT)
                            .where(CALIBRATION_RESULT.WORKER.eq(workerId))
                            .and(CALIBRATION_RESULT.ANSWER.in(possibleAnswers))
            );
            if (!alreadyAnswered) {
                create.insertInto(CALIBRATION_RESULT)
                        .set(new CalibrationResultRecord(null, workerId, answerOption))
                        .execute();
            }
            return alreadyAnswered;
        });
        if (answered)
            throw new IllegalArgumentException(String.format("Worker %d has already chosen an answer", workerId));
        return CalibrationAnswer.newBuilder()
                .setAnswerId(answerOption)
                .build();
    }
}
