package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.tools.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by marcel on 26.01.16.
 */
public class ExperimentTransformer {

    private static Map<String, String> convert(String templateData) {
        return null;
    }

    public static Experiment toProto(ExperimentRecord record, Experiment.State state,
                                     List<ConstraintRecord> constraintRecords,
                                     List<PopulationRecord> populationRecords,
                                     List<TagRecord> tagRecords) {
        return Experiment.newBuilder()
                .setId(record.getIdExperiment())
                .setDescription(record.getDescription())
                /*.set*/
                .setRatingsPerAnswer(record.getRatingsPerAnswer())
                /* */
                /* */
                .setAnswerType(AnswerType.valueOf(record.getAnswerType()))
                .setAlgorithmTaskChooser(record.getAlgorithmTaskChooser())
                .setAlgorithmQualityAnswer(record.getAlgorithmQualityAnswer())
                .setAlgorithmQualityRating(record.getAlgorithmQualityRating())
                .setPaymentBase(record.getBasePayment())
                .setPaymentAnswer(record.getBonusAnswer())
                .setPaymentRating(record.getBonusRating())
                .setState(state)
                .putAllPlaceholders(convert(record.getTemplateData()))
                .addAllConstraints(constraintRecords.stream().map(ConstraintsTransformer::toProto).collect(Collectors.toList()))
                .addAllPopulations(populationRecords.stream().map(PopulationTransformer::toProto).collect(Collectors.toList()))
                .addAllTags(tagRecords.stream().map(TagTransformer::toProto).collect(Collectors.toList()))
                .build();
    }


    public static ExperimentRecord toRecord(Experiment experiment) {
        return new ExperimentRecord(experiment.getId(),
                experiment.getTitle(),
                experiment.getDescription(),
                -1,
                experiment.getRatingsPerAnswer(),
                -1,
                -1,
                transform(experiment.getAnswerType()),
                experiment.getAlgorithmTaskChooser(),
                experiment.getAlgorithmQualityAnswer(),
                experiment.getAlgorithmQualityRating(),
                experiment.getPaymentBase(),
                experiment.getPaymentAnswer(),
                experiment.getPaymentRating(),
                (new JSONObject(experiment.getPlaceholders())).toString(),
                -1/*FIXME*/);
    }

    private static String transform(AnswerType answerType) {
        if (answerType == AnswerType.INVALID) return null;
        return answerType.name();
    }

    public static ExperimentRecord mergeProto(ExperimentRecord record, Experiment experiment) {
        return null;
    }
}
