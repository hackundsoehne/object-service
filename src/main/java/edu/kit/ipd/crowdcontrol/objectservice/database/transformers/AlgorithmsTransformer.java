package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * transforms the TaskChooser/AnswerQualityRatingQuality-Algorithms from the DB-records to the protobuf-definitions
 * @author LeanderK
 * @version 1.0
 */
public class AlgorithmsTransformer extends AbstractTransformer {

    /**
     * transforms the record into a AlgorithmOption
     * @param taskChooserRecord the taskChooserRecord
     * @param parameters the parameters
     * @return a list of AlgorithmOptions
     */
    public static AlgorithmOption constructTaskChooser(AlgorithmTaskChooserRecord taskChooserRecord, List<AlgorithmTaskChooserParamRecord> parameters) {
        HashMap<AlgorithmTaskChooserParamRecord, String> params = new HashMap<>();
        parameters.forEach(param -> params.put(param, null));
        return toTaskChooserProto(taskChooserRecord, params);
    }

    /**
     * transforms the record into a AlgorithmOption
     * @param answerQualityRecord the answerQualityRecord
     * @param parameters the parameters
     * @return a list of AlgorithmOptions
     */
    public static AlgorithmOption constructAnswerQuality(AlgorithmAnswerQualityRecord answerQualityRecord, List<AlgorithmAnswerQualityParamRecord> parameters) {
        HashMap<AlgorithmAnswerQualityParamRecord, String> params = new HashMap<>();
        parameters.forEach(param -> params.put(param, null));
        return toAnswerQualityProto(answerQualityRecord, params);
    }

    /**
     * transforms the record into a AlgorithmOption
     * @param ratingQuality the ratingQualityRecord
     * @param parameters the parameters
     * @return a list of AlgorithmOptions
     */
    public static AlgorithmOption constructRatingQuality(AlgorithmRatingQualityRecord ratingQuality, List<AlgorithmRatingQualityParamRecord> parameters) {
        HashMap<AlgorithmRatingQualityParamRecord, String> params = new HashMap<>();
        parameters.forEach(param -> params.put(param, null));
        return toRatingQualityProto(ratingQuality, params);
    }

    /**
     * creates the protobuf-representation for the TaskChooser
     * @param taskChooserRecord the TaskChooserRecord
     * @param taskChooserParams the parameters, where the values may be the chosen values
     * @return the resulting AlgorithmOption
     */
    static AlgorithmOption toTaskChooserProto(AlgorithmTaskChooserRecord taskChooserRecord,
                                              Map<AlgorithmTaskChooserParamRecord, String> taskChooserParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = taskChooserParams.entrySet().stream()
                .filter(entry -> entry.getKey().getIdAlgorithmTaskChooserParam() != null)
                .map(entry -> getParam(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(taskChooserRecord.getIdTaskChooser())
                .setDescription(taskChooserRecord.getDescription())
                .addAllParameters(parameters)
                .build();
    }

    /**
     * creates the protobuf-representation for the AnswerQuality-Algorithm
     * @param answerQualityRecord the the answerQualityRecord
     * @param answerQualityParams the parameters, where the values may be the chosen values
     * @return the resulting AlgorithmOption
     */
    static AlgorithmOption toAnswerQualityProto(AlgorithmAnswerQualityRecord answerQualityRecord,
                                                Map<AlgorithmAnswerQualityParamRecord, String> answerQualityParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = answerQualityParams.entrySet().stream()
                .filter(entry -> entry.getKey().getIdAlgorithmAnswerQualityParam() != null)
                .map(entry -> getParam(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(answerQualityRecord.getIdAlgorithmAnswerQuality())
                .setDescription(answerQualityRecord.getDescription())
                .addAllParameters(parameters)
                .build();
    }

    /**
     * creates the protobuf-representation for the RatingQuality-Algorithm
     * @param ratingQualityRecord the ratingQualityRecord
     * @param ratingQualityParams the parameters, where the values may be the chosen values
     * @return the resulting AlgorithmOption
     */
    static AlgorithmOption toRatingQualityProto(AlgorithmRatingQualityRecord ratingQualityRecord,
                                                Map<AlgorithmRatingQualityParamRecord, String> ratingQualityParams) {
        List<AlgorithmOption.AlgorithmParameter> parameters = ratingQualityParams.entrySet().stream()
                .filter(entry -> entry.getKey().getIdAlgorithmRatingQualityParam() != null)
                .map(entry -> getParam(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return AlgorithmOption.newBuilder()
                .setName(ratingQualityRecord.getIdAlgorithmRatingQuality())
                .setDescription(ratingQualityRecord.getDescription())
                .addAllParameters(parameters)
                .build();
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
}
