package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Map;
import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * this class is concerned with all the queries concerning the Algorithms-Tables
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmsOperations extends AbstractOperations {
    /**
     * creates a new AlgorithmsOperations
     *
     * @param create the context to use to communicate with the database
     */
    public AlgorithmsOperations(DSLContext create) {
        super(create);
    }

    /**
     * gets the TaskChooser-Algorithm with the passed id
     * @param id the primary key of the TaskChooser
     * @return the algorithm or empty if not found
     */
    public Optional<AlgorithmTaskChooserRecord> getTaskChooser(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return create.selectFrom(ALGORITHM_TASK_CHOOSER)
                .where(ALGORITHM_TASK_CHOOSER.ID_TASK_CHOOSER.eq(id))
                .fetchOptional();
    }

    /**
     * gets the RatingQuality-Algorithm with the passed id
     * @param id the primary key of the RatingQuality
     * @return the algorithm or empty or empty if not found
     */
    public Optional<AlgorithmRatingQualityRecord> getRatingQualityRecord(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return create.selectFrom(ALGORITHM_RATING_QUALITY)
                .where(ALGORITHM_RATING_QUALITY.ID_ALGORITHM_RATING_QUALITY.eq(id))
                .fetchOptional();
    }

    /**
     * gets the AnswerQuality-Algorithm with the passed id
     * @param id the primary key of the AnswerQuality
     * @return the algorithm or empty or empty if not found
     */
    public Optional<AlgorithmAnswerQualityRecord> getAnswerQualityRecord(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return create.selectFrom(ALGORITHM_ANSWER_QUALITY)
                .where(ALGORITHM_ANSWER_QUALITY.ID_ALGORITHM_ANSWER_QUALITY.eq(id))
                .fetchOptional();
    }

    /**
     * returns the TaskChooser-Algorithm parameters and the chosen value (or null if not yet chosen)
     * @param taskChooserID the primary key of the chosen taskChooser
     * @param experimentID the primary key of the experiment
     * @return a map where the values are the parameters and the keys the values
     */
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

    /**
     * returns the AnswerQuality-Algorithm parameters and the chosen value (or null if not yet chosen)
     * @param answerQualityID the primary key of the chosen AnswerQuality
     * @param experimentID the primary key of the experiment
     * @return a map where the values are the parameters and the keys the values
     */
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

    /**
     * returns the RatingQuality-Algorithm parameters and the chosen value (or null if not yet chosen)
     * @param ratingQualityID the primary key of the chosen RatingQuality
     * @param experimentID the primary key of the experiment
     * @return a map where the values are the parameters and the keys the values
     */
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

    /**
     * deletes all the chosen TaskChooser-Algorithm parameters for the passed experiment
     * @param experimentId the primary key of the experiment
     */
    public void deleteChosenTaskChooserParams(int experimentId) {
        create.deleteFrom(CHOSEN_TASK_CHOOSER_PARAM)
                .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(experimentId));
    }

    /**
     * deletes all the chosen AnswerQuality-Algorithm parameters for the passed experiment
     * @param experimentId the primary key of the experiment
     */
    public void deleteChosenAnswerQualityParams(int experimentId) {
        create.deleteFrom(CHOSEN_ANSWER_QUALITY_PARAM)
                .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(experimentId));
    }

    /**
     * deletes all the chosen RatingQuality-Algorithm parameters for the passed experiment
     * @param experimentId the primary key of the experiment
     */
    public void deleteChosenRatingQualityParams(int experimentId) {
        create.deleteFrom(CHOSEN_RATING_QUALITY_PARAM)
                .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(experimentId));
    }

    /**
     * stores the passed value as the chosen TaskChooser-Parameter
     * @param experimentID the primary key of the experiment
     * @param paramId the primary key of the parameter the value is referring to
     * @param value the actual value
     */
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

    /**
     * stores the passed value as the chosen AnswerQuality-Parameter
     * @param experimentID the primary key of the experiment
     * @param paramId the primary key of the parameter the value is referring to
     * @param value the actual value
     */
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

    /**
     * stores the passed value as the chosen RatingQuality-Parameter
     * @param experimentID the primary key of the experiment
     * @param paramId the primary key of the parameter the value is referring to
     * @param value the actual value
     */
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
