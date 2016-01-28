package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Map;
import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmsOperations extends AbstractOperations {
    /**
     * creates a new AbstractOperation
     *
     * @param create the context to use to communicate with the database
     */
    public AlgorithmsOperations(DSLContext create) {
        super(create);
    }

    public Optional<AlgorithmTaskChooserRecord> getTaskChooser(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return create.selectFrom(ALGORITHM_TASK_CHOOSER)
                .where(ALGORITHM_TASK_CHOOSER.ID_TASK_CHOOSER.eq(id))
                .fetchOptional();
    }

    public Optional<AlgorithmRatingQualityRecord> getRatingQualityRecord(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return create.selectFrom(ALGORITHM_RATING_QUALITY)
                .where(ALGORITHM_RATING_QUALITY.ID_ALGORITHM_RATING_QUALITY.eq(id))
                .fetchOptional();
    }

    public Optional<AlgorithmAnswerQualityRecord> getAnswerQualityRecord(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return create.selectFrom(ALGORITHM_ANSWER_QUALITY)
                .where(ALGORITHM_ANSWER_QUALITY.ID_ALGORITHM_ANSWER_QUALITY.eq(id))
                .fetchOptional();
    }

    public Map<AlgorithmTaskChooserParamRecord, String> getTaskChooserParams(String taskChooserID, int experimentID) {
        return create.select(ALGORITHM_TASK_CHOOSER_PARAM.fields())
                .select(CHOSEN_TASK_CHOOSER_PARAM.VALUE)
                .from(ALGORITHM_TASK_CHOOSER_PARAM)
                .leftJoin(CHOSEN_TASK_CHOOSER_PARAM).onKey()
                .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(experimentID))
                .and(ALGORITHM_TASK_CHOOSER_PARAM.ALGORITHM.eq(taskChooserID))
                .groupBy(ALGORITHM_TASK_CHOOSER_PARAM.fields())
                .fetchMap(ALGORITHM_TASK_CHOOSER_PARAM, record -> record.getValue(CHOSEN_TASK_CHOOSER_PARAM.VALUE));
    }

    public  Map<AlgorithmAnswerQualityParamRecord, String> getAnswerQualityParams(String answerQualityID, int experimentID) {
        return create.select(ALGORITHM_ANSWER_QUALITY_PARAM.fields())
                .select(CHOSEN_ANSWER_QUALITY_PARAM.VALUE)
                .from(ALGORITHM_ANSWER_QUALITY_PARAM)
                .leftJoin(CHOSEN_ANSWER_QUALITY_PARAM).onKey()
                .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(experimentID))
                .and(ALGORITHM_ANSWER_QUALITY_PARAM.ALGORITHM.eq(answerQualityID))
                .groupBy(ALGORITHM_ANSWER_QUALITY_PARAM.fields())
                .fetchMap(ALGORITHM_ANSWER_QUALITY_PARAM, record -> record.getValue(CHOSEN_ANSWER_QUALITY_PARAM.VALUE));
    }

    public Map<AlgorithmRatingQualityParamRecord, String> getRatingQualityParams(String ratingQualityID, int experimentID) {
        return create.select(ALGORITHM_RATING_QUALITY_PARAM.fields())
                .select(CHOSEN_RATING_QUALITY_PARAM.VALUE)
                .from(ALGORITHM_RATING_QUALITY_PARAM)
                .leftJoin(ALGORITHM_RATING_QUALITY_PARAM).onKey()
                .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(experimentID))
                .and(ALGORITHM_RATING_QUALITY_PARAM.ALGORITHM.eq(ratingQualityID))
                .groupBy(ALGORITHM_RATING_QUALITY_PARAM.fields())
                .fetchMap(ALGORITHM_RATING_QUALITY_PARAM, record -> record.getValue(CHOSEN_RATING_QUALITY_PARAM.VALUE));
    }

    public void deleteTaskChooserParams(int experimentId) {
        create.deleteFrom(CHOSEN_TASK_CHOOSER_PARAM)
                .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(experimentId));
    }

    public void deleteAnswerQualityParams(int experimentId) {
        create.deleteFrom(CHOSEN_ANSWER_QUALITY_PARAM)
                .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(experimentId));
    }

    public void deleteRatingQualityParams(int experimentId) {
        create.deleteFrom(CHOSEN_RATING_QUALITY_PARAM)
                .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(experimentId));
    }

    public void storeTaskChooserParam(int experimentID, int paramId, String value) {
        ChosenTaskChooserParamRecord record = new ChosenTaskChooserParamRecord(null, value, experimentID, paramId);
        create.transaction(config -> {
            Optional<ChosenTaskChooserParamRecord> existing = DSL.using(config).selectFrom(CHOSEN_TASK_CHOOSER_PARAM)
                    .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(experimentID))
                    .and(CHOSEN_TASK_CHOOSER_PARAM.PARAM.eq(paramId))
                    .fetchOptional();

            if (!existing.isPresent()) {
                DSL.using(config).executeInsert(record);
            } else if (!existing.get().getValue().equals(value)) {
                DSL.using(config).update(CHOSEN_TASK_CHOOSER_PARAM)
                        .set(record)
                        .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(experimentID))
                        .and(CHOSEN_TASK_CHOOSER_PARAM.PARAM.eq(paramId))
                        .execute();
            }
        });
    }

    public void storeAnswerQualityParam(int experimentID, int paramId, String value) {
        ChosenAnswerQualityParamRecord record = new ChosenAnswerQualityParamRecord(null, value, experimentID, paramId);
        create.transaction(config -> {
            Optional<ChosenAnswerQualityParamRecord> existing = DSL.using(config).selectFrom(CHOSEN_ANSWER_QUALITY_PARAM)
                    .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(experimentID))
                    .and(CHOSEN_ANSWER_QUALITY_PARAM.PARAM.eq(paramId))
                    .fetchOptional();

            if (!existing.isPresent()) {
                DSL.using(config).executeInsert(record);
            } else if (!existing.get().getValue().equals(value)) {
                DSL.using(config).update(CHOSEN_ANSWER_QUALITY_PARAM)
                        .set(record)
                        .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(experimentID))
                        .and(CHOSEN_ANSWER_QUALITY_PARAM.PARAM.eq(paramId))
                        .execute();
            }
        });
    }

    public void storeRatingQualityParam(int experimentID, int paramId, String value) {
        ChosenRatingQualityParamRecord record = new ChosenRatingQualityParamRecord(null, value, experimentID, paramId);
        create.transaction(config -> {
            Optional<ChosenRatingQualityParamRecord> existing = DSL.using(config).selectFrom(CHOSEN_RATING_QUALITY_PARAM)
                    .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(experimentID))
                    .and(CHOSEN_RATING_QUALITY_PARAM.PARAM.eq(paramId))
                    .fetchOptional();

            if (!existing.isPresent()) {
                DSL.using(config).executeInsert(record);
            } else if (!existing.get().getValue().equals(value)) {
                DSL.using(config).update(CHOSEN_RATING_QUALITY_PARAM)
                        .set(record)
                        .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(experimentID))
                        .and(CHOSEN_RATING_QUALITY_PARAM.PARAM.eq(paramId))
                        .execute();
            }
        });
    }
}
