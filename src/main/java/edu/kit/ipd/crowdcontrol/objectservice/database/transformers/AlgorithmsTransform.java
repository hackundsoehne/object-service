package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AlgorithmOption;
import org.jooq.Record;

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
     * this method expects the record to have the fields from table ALGORITHM_TASK_CHOOSER and
     * ALGORITHM_TASK_CHOOSER_PARAM.
     * @param records the list of records
     * @return a list of AlgorithmOptions
     */
    public static List<AlgorithmOption> constructTaskChooser(List<Record> records) {
        Function<Record, AlgorithmOption.AlgorithmParameter> getParam = record -> {
            AlgorithmTaskChooserParamRecord param = record.into(Tables.ALGORITHM_TASK_CHOOSER_PARAM);
            return AlgorithmOption.AlgorithmParameter.newBuilder()
                    .setDescription(param.getDescription())
                    .setId(param.getIdAlgorithmTaskChooserParam())
                    .setRegex(param.getRegex())
                    .build();
        };
        return records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.into(Tables.ALGORITHM_TASK_CHOOSER),
                        Collectors.mapping(getParam, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> AlgorithmOption.newBuilder()
                        .setName(entry.getKey().getIdTaskChooser())
                        .setDescription(entry.getKey().getDescription())
                        .addAllParameters(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * this method expects the record to have the fields from table ALGORITHM_TASK_CHOOSER and
     * ALGORITHM_TASK_CHOOSER_PARAM.
     * @param records the list of records
     * @return a list of AlgorithmOptions
     */
    public static List<AlgorithmOption> constructAnswerQuality(List<Record> records) {
        Function<Record, AlgorithmOption.AlgorithmParameter> getParam = record -> {
            AlgorithmAnswerQualityParamRecord param = record.into(Tables.ALGORITHM_ANSWER_QUALITY_PARAM);
            return AlgorithmOption.AlgorithmParameter.newBuilder()
                    .setDescription(param.getDescription())
                    .setId(param.getIdAlgorithmAnswerQualityParam())
                    .setRegex(param.getRegex())
                    .build();
        };
        return records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.into(Tables.ALGORITHM_ANSWER_QUALITY),
                        Collectors.mapping(getParam, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> AlgorithmOption.newBuilder()
                        .setName(entry.getKey().getIdAlgorithmAnswerQuality())
                        .setDescription(entry.getKey().getDescription())
                        .addAllParameters(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * this method expects the record to have the fields from table ALGORITHM_TASK_CHOOSER and
     * ALGORITHM_TASK_CHOOSER_PARAM.
     * @param records the list of records
     * @return a list of AlgorithmOptions
     */
    public static List<AlgorithmOption> constructRatingQuality(List<Record> records) {
        Function<Record, AlgorithmOption.AlgorithmParameter> getParam = record -> {
            AlgorithmRatingQualityParamRecord param = record.into(Tables.ALGORITHM_RATING_QUALITY_PARAM);
            return AlgorithmOption.AlgorithmParameter.newBuilder()
                    .setDescription(param.getDescription())
                    .setId(param.getIdAlgorithmRatingQualityParam())
                    .setRegex(param.getRegex())
                    .build();
        };
        return records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.into(Tables.ALGORITHM_RATING_QUALITY),
                        Collectors.mapping(getParam, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> AlgorithmOption.newBuilder()
                        .setName(entry.getKey().getIdAlgorithmRatingQuality())
                        .setDescription(entry.getKey().getDescription())
                        .addAllParameters(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }

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
