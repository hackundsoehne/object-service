package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Transforms the task chooser / answer quality / rating quality algorithms from the DB records to
 * the protobuf-definitions.
 *
 * @author Leander K.
 * @author Niklas Keller
 */
public class AlgorithmsTransformer extends AbstractTransformer {
    /**
     * Transforms the record into an AlgorithmOption.
     *
     * @param taskChooserRecord the task chooser record
     * @param parameters        the parameters
     *
     * @return A AlgorithmOption.
     */
    public static AlgorithmOption constructTaskChooser(AlgorithmTaskChooserRecord taskChooserRecord, List<AlgorithmTaskChooserParamRecord> parameters) {
        HashMap<AlgorithmTaskChooserParamRecord, String> params = new HashMap<>();
        parameters.forEach(param -> params.put(param, null));
        return toTaskChooserProto(taskChooserRecord, params);
    }

    /**
     * Transforms the record into an AlgorithmOption.
     *
     * @param answerQualityRecord the answer quality record
     * @param parameters          the parameters
     *
     * @return A AlgorithmOption.
     */
    public static AlgorithmOption constructAnswerQuality(AlgorithmAnswerQualityRecord answerQualityRecord, List<AlgorithmAnswerQualityParamRecord> parameters) {
        HashMap<AlgorithmAnswerQualityParamRecord, String> params = new HashMap<>();
        parameters.forEach(param -> params.put(param, null));
        return toAnswerQualityProto(answerQualityRecord, params);
    }

    /**
     * Transforms the record into an AlgorithmOption.
     *
     * @param ratingQuality the rating quality record
     * @param parameters    the parameters
     *
     * @return A AlgorithmOption.
     */
    public static AlgorithmOption constructRatingQuality(AlgorithmRatingQualityRecord ratingQuality, List<AlgorithmRatingQualityParamRecord> parameters) {
        HashMap<AlgorithmRatingQualityParamRecord, String> params = new HashMap<>();
        parameters.forEach(param -> params.put(param, null));
        return toRatingQualityProto(ratingQuality, params);
    }

    /**
     * Creates the protobuf representation for the task chooser.
     *
     * @param taskChooserRecord the task chooser record
     * @param taskChooserParams the parameters, where the values may be the chosen values
     *
     * @return Resulting AlgorithmOption.
     */
    public static AlgorithmOption toTaskChooserProto(AlgorithmTaskChooserRecord taskChooserRecord,
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
     * Creates the protobuf representation for the answer quality algorithm.
     *
     * @param answerQualityRecord the the answer quality record
     * @param answerQualityParams the parameters, where the values may be the chosen values
     *
     * @return Resulting AlgorithmOption.
     */
    public static AlgorithmOption toAnswerQualityProto(AlgorithmAnswerQualityRecord answerQualityRecord,
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
     * Creates the protobuf representation for the rating quality algorithm.
     *
     * @param ratingQualityRecord the rating quality record
     * @param ratingQualityParams the parameters, where the values may be the chosen values
     *
     * @return Resulting AlgorithmOption.
     */
    public static AlgorithmOption toRatingQualityProto(AlgorithmRatingQualityRecord ratingQualityRecord,
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
