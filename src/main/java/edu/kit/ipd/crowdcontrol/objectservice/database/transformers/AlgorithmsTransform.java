package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * transforms the TaskChooser/AnswerQualityRatingQuality-Algorithms from the DB-records to the protobuf-definitions
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmsTransform extends AbstractTransform {

    /**
     * transforms the records into TaskChoosers
     * @param algorithmRecords the map of records
     * @return a list of AlgorithmOptions
     */
    public static List<AlgorithmOption> constructTaskChoosers(Map<AlgorithmTaskChooserRecord, List<AlgorithmTaskChooserParamRecord>> algorithmRecords) {
        Function<List<AlgorithmTaskChooserParamRecord>, List<AlgorithmOption.AlgorithmParameter>> toParams = records ->
                records.stream()
                        .filter(record -> record.getIdAlgorithmTaskChooserParam() != null)
                        .map(record -> getParam(record, null))
                        .collect(Collectors.toList());

        return algorithmRecords.entrySet().stream()
                .map(entry ->
                        AlgorithmOption.newBuilder()
                        .setName(entry.getKey().getIdTaskChooser())
                        .setDescription(entry.getKey().getDescription())
                        .addAllParameters(toParams.apply(entry.getValue()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * transforms the records into Answer-Quality Algorithms
     * @param algorithmRecords the map of records
     * @return a list of Answer-Quality Algorithms
     */
    public static List<AlgorithmOption> constructAnswerQualityAlgorithms(Map<AlgorithmAnswerQualityRecord, List<AlgorithmAnswerQualityParamRecord>> algorithmRecords) {
        Function<List<AlgorithmAnswerQualityParamRecord>, List<AlgorithmOption.AlgorithmParameter>> toParams = records ->
                records.stream()
                        .filter(record -> record.getIdAlgorithmAnswerQualityParam() != null)
                        .map(record -> getParam(record, null))
                        .collect(Collectors.toList());

        return algorithmRecords.entrySet().stream()
                .map(entry ->
                        AlgorithmOption.newBuilder()
                                .setName(entry.getKey().getIdAlgorithmAnswerQuality())
                                .setDescription(entry.getKey().getDescription())
                                .addAllParameters(toParams.apply(entry.getValue()))
                                .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * transforms the records into Rating-Quality Algorithms
     * @param algorithmRecords the map of records
     * @return a list of Rating-Quality Algorithms
     */
    public static List<AlgorithmOption> constructRatingQualityAlgorithms(Map<AlgorithmRatingQualityRecord, List<AlgorithmRatingQualityParamRecord>> algorithmRecords) {
        Function<List<AlgorithmRatingQualityParamRecord>, List<AlgorithmOption.AlgorithmParameter>> toParams = records ->
                records.stream()
                        .filter(record -> record.getIdAlgorithmRatingQualityParam() != null)
                        .map(record -> getParam(record, null))
                        .collect(Collectors.toList());

        return algorithmRecords.entrySet().stream()
                .map(entry ->
                        AlgorithmOption.newBuilder()
                                .setName(entry.getKey().getIdAlgorithmRatingQuality())
                                .setDescription(entry.getKey().getDescription())
                                .addAllParameters(toParams.apply(entry.getValue()))
                                .build()
                )
                .collect(Collectors.toList());
    }

    private static AlgorithmOption.AlgorithmParameter getParam(AlgorithmTaskChooserParamRecord record, String value) {
        AlgorithmOption.AlgorithmParameter.Builder builder = AlgorithmOption.AlgorithmParameter.newBuilder()
                .setDescription(record.getDescription())
                .setId(record.getIdAlgorithmTaskChooserParam())
                .setRegex(record.getRegex());
        if (value != null) {
            builder = builder.setValue(value);
        }
        return builder
                .build();
    }

    private static AlgorithmOption.AlgorithmParameter getParam(AlgorithmAnswerQualityParamRecord record, String value) {
        AlgorithmOption.AlgorithmParameter.Builder builder = AlgorithmOption.AlgorithmParameter.newBuilder()
                .setDescription(record.getDescription())
                .setId(record.getIdAlgorithmAnswerQualityParam())
                .setRegex(record.getRegex());
        if (value != null) {
            builder = builder.setValue(value);
        }
        return builder
                .build();
    }

    private static AlgorithmOption.AlgorithmParameter getParam(AlgorithmRatingQualityParamRecord record, String value) {
        AlgorithmOption.AlgorithmParameter.Builder builder = AlgorithmOption.AlgorithmParameter.newBuilder()
                .setDescription(record.getDescription())
                .setId(record.getIdAlgorithmRatingQualityParam())
                .setRegex(record.getRegex());
        if (value != null) {
            builder = builder.setValue(value);
        }
        return builder
                .build();
    }

    static AlgorithmOption toTaskChooserProto(AlgorithmTaskChooserRecord taskChooserRecord,
                                                      Map<AlgorithmTaskChooserParamRecord, String> taskChooserParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = taskChooserParams.entrySet().stream()
                .map(entry -> getParam(entry.getKey(), entry.getValue()))
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
                .map(entry -> getParam(entry.getKey(), entry.getValue()))
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
                .map(entry -> getParam(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(ratingQualityRecord.getIdAlgorithmRatingQuality())
                .setDescription(ratingQualityRecord.getDescription())
                .addAllParameters(parameters)
                .build();
    }
}
