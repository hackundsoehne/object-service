package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.tools.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by marcel on 26.01.16.
 */
public class ExperimentTransform extends AbstractTransform {
    /**
     * Convert a experiment record to a proto object with the given additional infos
     * @param record The Database record to use
     * @param state state of the experiment
     * @return the experiment object with the given data
     */
    public static Experiment toProto(ExperimentRecord record, Experiment.State state) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();

        return Experiment.newBuilder()
                .setId(record.getIdExperiment())
                .setDescription(record.getDescription())
                .setNeededAnswers(record.getNeededAnswers())
                .setRatingsPerAnswer(record.getRatingsPerAnswer())
                .setAnswersPerWorker(record.getAnwersPerWorker())
                .setRatingsPerWorker(record.getRatingsPerWorker())
                .setAnswerType(AnswerType.valueOf(record.getAnswerType()))
                .setAlgorithmTaskChooser(AlgorithmOption.newBuilder().setName(record.getAlgorithmTaskChooser()).build())
                .setAlgorithmQualityAnswer(AlgorithmOption.newBuilder().setName(record.getAlgorithmQualityAnswer()).build())
                .setAlgorithmQualityRating(AlgorithmOption.newBuilder().setName(record.getAlgorithmQualityRating()).build())
                .setPaymentBase(record.getBasePayment())
                .setPaymentAnswer(record.getBonusAnswer())
                .setPaymentRating(record.getBonusRating())
                .setState(state)
                .putAllPlaceholders(new Gson().fromJson(record.getTemplateData(), type))
                .build();
    }

    /**
     * Convert a experiment record to a proto object with the given additional infos
     * @param record The Database record to use
     * @param state state of the experiment
     * @param constraintRecords constraints of a experiment
     * @param platforms calibrations on the platform to use
     * @param tagRecords tags which are saved for a experiment
     * @return the experiment object with the given data
     */
    public static Experiment toProto(ExperimentRecord record, Experiment.State state,
                                     List<ConstraintRecord> constraintRecords,
                                     List<Experiment.Population> platforms,
                                     List<TagRecord> tagRecords,
                                     AlgorithmTaskChooserRecord taskChooserRecord,
                                     Map<AlgorithmTaskChooserParamRecord, String> taskChooserParams,
                                     AlgorithmAnswerQualityRecord answerQualityRecord,
                                     Map<AlgorithmAnswerQualityParamRecord, String> answerQualityParams,
                                     AlgorithmRatingQualityRecord ratingQualityRecord,
                                     Map<AlgorithmRatingQualityParamRecord, String> ratingQualityParams) {
        return toProto(record, state).toBuilder()
                .setAlgorithmTaskChooser(AlgorithmsTransform.toTaskChooserProto(taskChooserRecord, taskChooserParams))
                .setAlgorithmQualityAnswer(AlgorithmsTransform.toAnswerQualityProto(answerQualityRecord, answerQualityParams))
                .setAlgorithmQualityRating(AlgorithmsTransform.toRatingQualityProto(ratingQualityRecord, ratingQualityParams))
                .addAllConstraints(constraintRecords.stream().map(TagConstraintTransform::toConstraintsProto).collect(Collectors.toList()))
                .addAllPopulations(platforms)
                .addAllTags(tagRecords.stream().map(TagConstraintTransform::toTagProto).collect(Collectors.toList()))
                .build();
    }

    /**
     * Creates a new record  from the data of a experiment
     * @param experiment the protobuf to generate the record from
     * @return a record
     */
    public static ExperimentRecord toRecord(Experiment experiment) {
        return new ExperimentRecord(experiment.getId(),
                experiment.getTitle(),
                experiment.getDescription(),
                experiment.getNeededAnswers(),
                experiment.getRatingsPerAnswer(),
                experiment.getAnswersPerWorker(),
                experiment.getRatingsPerWorker(),
                transform(experiment.getAnswerType()),
                experiment.getAlgorithmTaskChooser().getName(),
                experiment.getAlgorithmQualityAnswer().getName(),
                experiment.getAlgorithmQualityRating().getName(),
                experiment.getPaymentBase(),
                experiment.getPaymentAnswer(),
                experiment.getPaymentRating(),
                (new JSONObject(experiment.getPlaceholders())).toString(),
                experiment.getTemplateId(),
                experiment.getWorkerQualityThreshold());
    }

    private static String transform(AnswerType answerType) {
        if (answerType == AnswerType.INVALID) return null;
        return answerType.name();
    }

    /**
     * Merge the data from a experiment proto object into a existing record
     * @param record_ The original record to merge into
     * @param experiment the experiment to merge
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
                    record.setAnwersPerWorker(experiment.getAnswersPerWorker());
                    break;
                case Experiment.CONSTRAINTS_FIELD_NUMBER:
                    // has to be done manual with Constraints Transformer
                    break;
                case Experiment.DESCRIPTION_FIELD_NUMBER:
                    record.setDescription(experiment.getDescription());
                    break;
                case Experiment.ID_FIELD_NUMBER:
                    record.setIdExperiment(experiment.getId());
                    break;
                case Experiment.PAYMENT_ANSWER_FIELD_NUMBER:
                    record.setBonusAnswer(experiment.getPaymentAnswer());
                    break;
                case Experiment.PAYMENT_BASE_FIELD_NUMBER:
                    record.setBasePayment(experiment.getPaymentBase());
                    break;
                case Experiment.PAYMENT_RATING_FIELD_NUMBER:
                    record.setBonusRating(experiment.getPaymentRating());
                    break;
                case Experiment.PLACEHOLDERS_FIELD_NUMBER:
                    record.setTemplateData(new JSONObject(experiment.getPlaceholders()).toString());
                    break;
                case Experiment.POPULATIONS_FIELD_NUMBER:
                    // has to be done manual with CalibrationsTransformer
                    break;
                case Experiment.RATINGS_PER_ANSWER_FIELD_NUMBER:
                    record.setRatingsPerAnswer(experiment.getRatingsPerAnswer());
                    break;
                case Experiment.STATE_FIELD_NUMBER:
                    // this is not merged into the database this event will
                    // start the platforms to populate this experiment
                    // which results in entrys in the database which will mark the experiment as published
                    break;
                case Experiment.TAGS_FIELD_NUMBER:
                    // has to be done manual with TagConstraintTransform
                    break;
                case Experiment.TEMPLATE_ID_FIELD_NUMBER:
                    record.setTemplate(experiment.getTemplateId());
                    break;
                case Experiment.TITLE_FIELD_NUMBER:
                    record.setTitel(experiment.getTitle());
                    break;
            }
        });
    }
}
