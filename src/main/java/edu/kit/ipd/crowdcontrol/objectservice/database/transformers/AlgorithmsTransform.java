package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * transforms the TaskChooser/AnswerQualityRatingQuality-Algorithms from the DB-records to the protobuf-definitions
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmsTransform extends AbstractTransform {

    static AlgorithmOption toTaskChooserProto(AlgorithmTaskChooserRecord taskChooserRecord,
                                                      Map<AlgorithmTaskChooserParamRecord, String> taskChooserParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = taskChooserParams.entrySet().stream()
                .map(entry -> AlgorithmOption.AlgorithmParameter.newBuilder()
                        .setDescription(entry.getKey().getDescription())
                        .setId(entry.getKey().getIdAlgorithmTaskChooserParam())
                        .setRegex(entry.getKey().getRegex())
                        .setValue(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(taskChooserRecord.getIdTaskChooser())
                .setDescription(taskChooserRecord.getDescription())
                .addAllParameters(parameters)
                .build();
    }

    static AlgorithmOption toAnswerQualityProto(AlgorithmAnswerQualityRecord answerQualityRecord,
                                                        Map<AlgorithmAnswerQualityParamRecord, String> answerQualityParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = answerQualityParams.entrySet().stream()
                .map(entry -> AlgorithmOption.AlgorithmParameter.newBuilder()
                        .setDescription(entry.getKey().getDescription())
                        .setId(entry.getKey().getIdAlgorithmAnswerQualityParam())
                        .setRegex(entry.getKey().getRegex())
                        .setValue(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(answerQualityRecord.getIdAlgorithmAnswerQuality())
                .setDescription(answerQualityRecord.getDescription())
                .addAllParameters(parameters)
                .build();
    }

    static AlgorithmOption toRatingQualityProto(AlgorithmRatingQualityRecord ratingQualityRecord,
                                                        Map<AlgorithmRatingQualityParamRecord, String> ratingQualityParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = ratingQualityParams.entrySet().stream()
                .map(entry -> AlgorithmOption.AlgorithmParameter.newBuilder()
                        .setDescription(entry.getKey().getDescription())
                        .setId(entry.getKey().getIdAlgorithmRatingQualityParam())
                        .setRegex(entry.getKey().getRegex())
                        .setValue(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(ratingQualityRecord.getIdAlgorithmRatingQuality())
                .setDescription(ratingQualityRecord.getDescription())
                .addAllParameters(parameters)
                .build();
    }
}
