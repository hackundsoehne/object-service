package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionTemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms template protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class TemplateTransformer extends AbstractTransformer {
    /**
     * Converts a template record to its protobuf representation.
     *
     * @param record template record
     * @return Template.
     */
    public static Template toProto(TemplateRecord record) {
        return toProto(record, Collections.emptyList());
    }
    /**
     * Converts a template record to its protobuf representation.
     *
     * @param record template record
     * @param ratingOptions the ratingOptions
     * @return Template.
     */
    public static Template toProto(TemplateRecord record, List<RatingOptionTemplateRecord> ratingOptions) {
        AnswerType answerType = "IMAGE".equals(record.getAnswerType())
                ? AnswerType.IMAGE
                : AnswerType.TEXT;

        List<Template.RatingOption> options = ratingOptions.stream()
                .map(option -> Template.RatingOption.newBuilder()
                        .setTemplateRatingId(option.getIdRatingOptionsTemplate())
                        .setName(option.getName())
                        .setValue(option.getValue())
                        .build()
                )
                .collect(Collectors.toList());

        return Template.newBuilder()
                .setId(record.getIdTemplate())
                .setName(record.getTitel())
                .setContent(record.getTemplate())
                .setAnswerType(answerType)
                .addAllRatingOptions(options)
                .build();
    }

    public static RatingOptionTemplateRecord toRecord(Template.RatingOption ratingOption, int templateId) {
        RatingOptionTemplateRecord optionRecord = new RatingOptionTemplateRecord();
        optionRecord.setTemplate(templateId);
        return merge(optionRecord, ratingOption, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Template.RatingOption.TEMPLATE_RATING_ID_FIELD_NUMBER: record.setIdRatingOptionsTemplate(ratingOption.getTemplateRatingId());
                    break;
                case Template.RatingOption.NAME_FIELD_NUMBER: record.setName(ratingOption.getName());
                    break;
                case Template.RatingOption.VALUE_FIELD_NUMBER: record.setValue(ratingOption.getValue());
                    break;
            }
        });
    }

    /**
     * Merges a record with the set properties of a protobuf template.
     *
     * @param target record to merge into
     * @param template message to merge from
     *
     * @return Merged template record.
     */
    public static TemplateRecord mergeRecord(TemplateRecord target, Template template) {
        return merge(target, template, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Template.NAME_FIELD_NUMBER: record.setTitel(template.getName());
                    break;
                case Template.CONTENT_FIELD_NUMBER: record.setTemplate(template.getContent());
                    break;
                case Template.ANSWER_TYPE_FIELD_NUMBER:
                    if ("IMAGE".equals(template.getAnswerType().name())) {
                        record.setAnswerType(template.getAnswerType().name());
                    }
                    break;
            }
        });
    }
}
