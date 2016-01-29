package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AlgorithmsTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * this class is concerned with all the queries concerning the Algorithm-Tables
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmOperations extends AbstractOperations {
    /**
     * creates a new AlgorithmsOperations
     *
     * @param create the context to use to communicate with the database
     */
    public AlgorithmOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of TaskChooser-Algorithms starting from {@code cursor}.
     *
     * @param cursor Pagination cursor or null if first item
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     * @return List of TaskChooser-Algorithms
     */
    public Range<AlgorithmOption, String> getTaskChoosersFrom(String cursor, boolean next, int limit) {
        SelectJoinStep<Record1<String>> query = create.select(ALGORITHM_TASK_CHOOSER.ID_TASK_CHOOSER).from(ALGORITHM_TASK_CHOOSER);
        return getNextRange(query, ALGORITHM_TASK_CHOOSER.ID_TASK_CHOOSER, ALGORITHM_TASK_CHOOSER,
                cursor, next, limit, String::compareTo)
                .mapList(records -> AlgorithmsTransformer.constructTaskChoosers(getTaskChooserParams(records)));
    }

    private Map<AlgorithmTaskChooserRecord, List<AlgorithmTaskChooserParamRecord>> getTaskChooserParams(List<Record1<String>> taskChooserIds) {
        return create.select(ALGORITHM_TASK_CHOOSER.fields())
                .select(ALGORITHM_TASK_CHOOSER_PARAM.fields())
                .from(ALGORITHM_TASK_CHOOSER)
                .leftJoin(ALGORITHM_TASK_CHOOSER_PARAM).onKey()
                .where(ALGORITHM_TASK_CHOOSER.ID_TASK_CHOOSER.in(flatten(taskChooserIds)))
                .fetchGroups(ALGORITHM_TASK_CHOOSER, record -> record.into(ALGORITHM_TASK_CHOOSER_PARAM));
    }

    /**
     * Returns a range of AnswerQuality-Algorithms starting from {@code cursor}.
     *
     * @param cursor Pagination cursor or null if first item
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     * @return List of AnswerQuality-Algorithms
     */
    public Range<AlgorithmOption, String> getAnswerQualityAlgorithmsFrom(String cursor, boolean next, int limit) {
        SelectJoinStep<Record1<String>> query = create.select(ALGORITHM_ANSWER_QUALITY.ID_ALGORITHM_ANSWER_QUALITY).from(ALGORITHM_ANSWER_QUALITY);
        return getNextRange(query, ALGORITHM_ANSWER_QUALITY.ID_ALGORITHM_ANSWER_QUALITY, ALGORITHM_ANSWER_QUALITY,
                cursor, next, limit, String::compareTo)
                .mapList(records -> AlgorithmsTransformer.constructAnswerQualityAlgorithms(getAnswerQualityParams(records)));
    }

    private Map<AlgorithmAnswerQualityRecord, List<AlgorithmAnswerQualityParamRecord>> getAnswerQualityParams(List<Record1<String>> answerQualityIds) {
        return create.select(ALGORITHM_ANSWER_QUALITY.fields())
                .select(ALGORITHM_ANSWER_QUALITY_PARAM.fields())
                .from(ALGORITHM_ANSWER_QUALITY)
                .leftJoin(ALGORITHM_ANSWER_QUALITY_PARAM).onKey()
                .where(ALGORITHM_ANSWER_QUALITY.ID_ALGORITHM_ANSWER_QUALITY.in(flatten(answerQualityIds)))
                .fetchGroups(ALGORITHM_ANSWER_QUALITY, record -> record.into(ALGORITHM_ANSWER_QUALITY_PARAM));
    }

    /**
     * Returns a range of RatingQuality-Algorithms starting from {@code cursor}.
     *
     * @param cursor Pagination cursor or null if first item
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     * @return List of RatingQuality-Algorithms
     */
    public Range<AlgorithmOption, String> getRatingQualityAlgorithmsFrom(String cursor, boolean next, int limit) {
        SelectJoinStep<Record1<String>> query = create.select(ALGORITHM_RATING_QUALITY.ID_ALGORITHM_RATING_QUALITY).from(ALGORITHM_RATING_QUALITY);
        return getNextRange(query, ALGORITHM_RATING_QUALITY.ID_ALGORITHM_RATING_QUALITY, ALGORITHM_RATING_QUALITY,
                cursor, next, limit, String::compareTo)
                .mapList(records -> AlgorithmsTransformer.constructRatingQualityAlgorithms(getRatingQualityParams(records)));
    }

    private Map<AlgorithmRatingQualityRecord, List<AlgorithmRatingQualityParamRecord>> getRatingQualityParams(List<Record1<String>> ratingQualityIDs) {
        return create.select(ALGORITHM_RATING_QUALITY.fields())
                .select(ALGORITHM_RATING_QUALITY_PARAM.fields())
                .from(ALGORITHM_RATING_QUALITY)
                .leftJoin(ALGORITHM_RATING_QUALITY_PARAM).onKey()
                .where(ALGORITHM_RATING_QUALITY.ID_ALGORITHM_RATING_QUALITY.in(flatten(ratingQualityIDs)))
                .fetchGroups(ALGORITHM_RATING_QUALITY, record -> record.into(ALGORITHM_RATING_QUALITY_PARAM));
    }

    private List<String> flatten(List<Record1<String>> taskChooser) {
        return taskChooser.stream()
                .map(Record1::value1)
                .collect(Collectors.toList());
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
                .leftJoin(CHOSEN_RATING_QUALITY_PARAM).onKey()
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
                .where(CHOSEN_TASK_CHOOSER_PARAM.EXPERIMENT.eq(experimentId))
                .execute();
    }

    /**
     * deletes all the chosen AnswerQuality-Algorithm parameters for the passed experiment
     * @param experimentId the primary key of the experiment
     */
    public void deleteChosenAnswerQualityParams(int experimentId) {
        create.deleteFrom(CHOSEN_ANSWER_QUALITY_PARAM)
                .where(CHOSEN_ANSWER_QUALITY_PARAM.EXPERIMENT.eq(experimentId))
                .execute();
    }

    /**
     * deletes all the chosen RatingQuality-Algorithm parameters for the passed experiment
     * @param experimentId the primary key of the experiment
     */
    public void deleteChosenRatingQualityParams(int experimentId) {
        create.deleteFrom(CHOSEN_RATING_QUALITY_PARAM)
                .where(CHOSEN_RATING_QUALITY_PARAM.EXPERIMENT.eq(experimentId))
                .execute();
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
