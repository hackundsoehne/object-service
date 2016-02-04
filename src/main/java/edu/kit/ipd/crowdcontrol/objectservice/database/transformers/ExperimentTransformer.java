package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.tools.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles transformations of experiments from and to the protocol buffer messages.
 *
 * @author Leander K.
 * @author Marcel Hollderbach
 * @author Niklas Keller
 */
public class ExperimentTransformer extends AbstractTransformer {
    /**
     * Convert a experiment record to a proto object with the given additional information.
     *
     * @param record The Database record to use
     * @param state  state of the experiment
     *
     * @return the experiment object with the given data
     */
    public static Experiment toProto(ExperimentRecord record, Experiment.State state) {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Function<String, AlgorithmOption> algo = name -> AlgorithmOption.newBuilder().setName(name).build();

        return builder(Experiment.newBuilder())
                .set(record.getIdExperiment(), Experiment.Builder::setId)
                .set(record.getTitle(), Experiment.Builder::setTitle)
                .set(record.getDescription(), Experiment.Builder::setDescription)
                .set(state, Experiment.Builder::setState)
                .set(record.getAnswerType(), (builder, x) -> builder.setAnswerType(AnswerType.valueOf(x)))
                .set(record.getAlgorithmTaskChooser(), (builder, x) -> builder.setAlgorithmTaskChooser(algo.apply(x)))
                .set(record.getAlgorithmQualityAnswer(), (builder, x) -> builder.setAlgorithmQualityAnswer(algo.apply(x)))
                .set(record.getAlgorithmQualityRating(), (builder, x) -> builder.setAlgorithmQualityRating(algo.apply(x)))
                .set(toInteger(record.getAnwersPerWorker()), Experiment.Builder::setAnswersPerWorker)
                .set(toInteger(record.getRatingsPerWorker()), Experiment.Builder::setRatingsPerWorker)
                .set(toInteger(record.getRatingsPerAnswer()), Experiment.Builder::setRatingsPerAnswer)
                .set(toInteger(record.getNeededAnswers()), Experiment.Builder::setNeededAnswers)
                .set(toInteger(record.getBasePayment()), Experiment.Builder::setPaymentBase)
                .set(toInteger(record.getBonusAnswer()), Experiment.Builder::setPaymentAnswer)
                .set(toInteger(record.getBonusRating()), Experiment.Builder::setPaymentRating)
                .set(record.getTemplateData(), ((builder, s) -> builder.putAllPlaceholders(new Gson().fromJson(s, type))))
                .set(toInteger(record.getWorkerQualityThreshold()), Experiment.Builder::setWorkerQualityThreshold)
                .set(toInteger(record.getTemplate()), Experiment.Builder::setTemplateId)
                .getBuilder()
                .build();
    }

    /**
     * Convert a experiment record to a proto object with the given additional infos
     *
     * @param record            The Database record to use
     * @param state             state of the experiment
     * @param constraintRecords constraints of a experiment
     * @param platforms         calibrations on the platform to use
     * @param tagRecords        tags which are saved for a experiment
     *
     * @return the experiment object with the given data
     */
    public static Experiment toProto(ExperimentRecord record, Experiment.State state,
                                     List<ConstraintRecord> constraintRecords,
                                     List<Experiment.Population> platforms,
                                     List<TagRecord> tagRecords,
                                     List<RatingOptionExperimentRecord> ratingOptions,
                                     AlgorithmOption taskChooser,
                                     AlgorithmOption answerQuality,
                                     AlgorithmOption ratingQuality) {
        List<Constraint> constraints = constraintRecords.stream()
                .map(TagConstraintTransformer::toConstraintsProto)
                .collect(Collectors.toList());
        return builder(toProto(record, state).toBuilder())
                .set(taskChooser, Experiment.Builder::setAlgorithmTaskChooser)
                .set(answerQuality, Experiment.Builder::setAlgorithmQualityAnswer)
                .set(ratingQuality, Experiment.Builder::setAlgorithmQualityRating)
                .getBuilder()
                .addAllConstraints(constraints)
                .addAllPopulations(platforms)
                .addAllTags(tagRecords.stream().map(TagConstraintTransformer::toTagProto).collect(Collectors.toList()))
                .addAllRatingOptions(ratingOptions.stream().map(ExperimentTransformer::transform).collect(Collectors.toList()))
                .build();
    }

    private static String transform(AnswerType answerType) {
        if (answerType == AnswerType.INVALID) return null;
        return answerType.name();
    }

    private static Experiment.RatingOption transform(RatingOptionExperimentRecord record) {
        return Experiment.RatingOption.newBuilder()
                .setExperimentRatingId(record.getIdRatingOptionExperiment())
                .setName(record.getName())
                .setValue(record.getValue())
                .build();
    }

    /**
     * Merge the data from a experiment proto object into a existing record
     *
     * @param record_    The original record to merge into
     * @param experiment the experiment to merge
     *
     * @return A merged experiment record
     */
    public static ExperimentRecord mergeProto(ExperimentRecord record_, Experiment experiment) {
        return merge(record_, experiment, (integer, record) -> {
            switch (integer) {
                case Experiment.ALGORITHM_QUALITY_ANSWER_FIELD_NUMBER:
                    //the parameters have to be done with the AlgorithmOptionTransform
                    record.setAlgorithmQualityAnswer(experiment.getAlgorithmQualityAnswer().getName());
                    break;
                case Experiment.ALGORITHM_QUALITY_RATING_FIELD_NUMBER:
                    //the parameters have to be done with the AlgorithmOptionTransform
                    record.setAlgorithmQualityRating(experiment.getAlgorithmQualityRating().getName());
                    break;
                case Experiment.ALGORITHM_TASK_CHOOSER_FIELD_NUMBER:
                    //the parameters have to be done with the AlgorithmOptionTransform
                    record.setAlgorithmTaskChooser(experiment.getAlgorithmTaskChooser().getName());
                    break;
                case Experiment.ANSWER_TYPE_FIELD_NUMBER:
                    record.setAnswerType(transform(experiment.getAnswerType()));
                    break;
                case Experiment.ANSWERS_PER_WORKER_FIELD_NUMBER:
                    record.setAnwersPerWorker(experiment.getAnswersPerWorker().getValue());
                    break;
                case Experiment.CONSTRAINTS_FIELD_NUMBER:
                    // has to be done manual with Constraints Transformer
                    break;
                case Experiment.DESCRIPTION_FIELD_NUMBER:
                    record.setDescription(experiment.getDescription());
                    break;
                case Experiment.PAYMENT_ANSWER_FIELD_NUMBER:
                    record.setBonusAnswer(experiment.getPaymentAnswer().getValue());
                    break;
                case Experiment.PAYMENT_BASE_FIELD_NUMBER:
                    record.setBasePayment(experiment.getPaymentBase().getValue());
                    break;
                case Experiment.PAYMENT_RATING_FIELD_NUMBER:
                    record.setBonusRating(experiment.getPaymentRating().getValue());
                    break;
                case Experiment.PLACEHOLDERS_FIELD_NUMBER:
                    if (experiment.getPlaceholders().size() == 0) {
                        break;
                    }

                    record.setTemplateData(new JSONObject(experiment.getPlaceholders()).toString());
                    break;
                case Experiment.POPULATIONS_FIELD_NUMBER:
                    // has to be done manual with CalibrationsTransformer
                    break;
                case Experiment.RATINGS_PER_ANSWER_FIELD_NUMBER:
                    record.setRatingsPerAnswer(experiment.getRatingsPerAnswer().getValue());
                    break;
                case Experiment.STATE_FIELD_NUMBER:
                    // this is not merged into the database this event will
                    // start the platforms to populate this experiment
                    // which results in entries in the database which will mark the experiment as published
                    break;
                case Experiment.TAGS_FIELD_NUMBER:
                    // has to be done manual with TagConstraintTransform
                    break;
                case Experiment.TEMPLATE_ID_FIELD_NUMBER:
                    record.setTemplate(experiment.getTemplateId().getValue());
                    break;
                case Experiment.TITLE_FIELD_NUMBER:
                    record.setTitle(experiment.getTitle());
                    break;
                case Experiment.NEEDED_ANSWERS_FIELD_NUMBER:
                    record.setNeededAnswers(experiment.getNeededAnswers().getValue());
                    break;
                case Experiment.RATINGS_PER_WORKER_FIELD_NUMBER:
                    record.setRatingsPerWorker(experiment.getRatingsPerWorker().getValue());
                    break;
                case Experiment.WORKER_QUALITY_THRESHOLD_FIELD_NUMBER:
                    record.setWorkerQualityThreshold(experiment.getWorkerQualityThreshold().getValue());
                    break;
            }
        });
    }

    /**
     * creates a list of RatingOptionExperimentRecords from the passed experiment
     *
     * @param experiment the Experiment
     *
     * @return a list of RatingOptionExperimentRecords
     */
    public static List<RatingOptionExperimentRecord> toRecord(Experiment experiment) {
        return experiment.getRatingOptionsList().stream()
                .map(ratingOption ->
                                merge(new RatingOptionExperimentRecord(), ratingOption, (field, record) -> {
                                    switch (field) {
                                        case Experiment.RatingOption.EXPERIMENT_RATING_ID_FIELD_NUMBER:
                                            record.setIdRatingOptionExperiment(ratingOption.getExperimentRatingId());
                                            break;
                                        case Experiment.RatingOption.NAME_FIELD_NUMBER:
                                            record.setName(ratingOption.getName());
                                            break;
                                        case Experiment.RatingOption.VALUE_FIELD_NUMBER:
                                            record.setValue(ratingOption.getValue());
                                            break;
                                    }
                                })
                )
                .collect(Collectors.toList());
    }
}
