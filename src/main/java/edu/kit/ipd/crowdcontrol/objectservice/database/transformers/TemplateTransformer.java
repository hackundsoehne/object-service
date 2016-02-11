package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionTemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateTagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;
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
        return toProto(record, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
    /**
     * Converts a template record to its protobuf representation.
     *
     * @param record template record
     * @param ratingOptions the ratingOptions
     * @return Template.
     */
    public static Template toProto(TemplateRecord record, List<RatingOptionTemplateRecord> ratingOptions, List<TemplateTagRecord> tags, List<TemplateConstraintRecord> constraints) {
        AnswerType answerType = record.getAnswerType() == null
                ? AnswerType.TEXT
                : AnswerType.IMAGE;

        List<Template.RatingOption> options = ratingOptions.stream()
                .map(option -> Template.RatingOption.newBuilder()
                        .setName(option.getName())
                        .setValue(option.getValue())
                        .build()
                )
                .collect(Collectors.toList());

        List<Tag> tagList = tags.stream()
                .map(tag -> Tag.newBuilder().setName(tag.getTag()).build())
                .collect(Collectors.toList());

        List<Constraint> constraintList = constraints.stream()
                .map(constraint -> Constraint.newBuilder().setName(constraint.getConstraint()).build())
                .collect(Collectors.toList());

        return Template.newBuilder()
                .setId(record.getIdTemplate())
                .setName(record.getTitle())
                .setContent(record.getTemplate())
                .setAnswerType(answerType)
                .addAllRatingOptions(options)
                .addAllTags(tagList)
                .addAllConstraints(constraintList)
                .build();
    }

    public static RatingOptionTemplateRecord toRecord(Template.RatingOption ratingOption, int templateId) {
        RatingOptionTemplateRecord optionRecord = new RatingOptionTemplateRecord();
        optionRecord.setTemplate(templateId);

        return merge(optionRecord, ratingOption, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Template.RatingOption.NAME_FIELD_NUMBER:
                    record.setName(ratingOption.getName());
                    break;
                case Template.RatingOption.VALUE_FIELD_NUMBER:
                    record.setValue(ratingOption.getValue());
                    break;
            }
        });
    }

    public static TemplateTagRecord toRecord(Tag tag, int templateId) {
        TemplateTagRecord tagRecord = new TemplateTagRecord();
        tagRecord.setTemplate(templateId);

        return merge(tagRecord, tag, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Tag.NAME_FIELD_NUMBER:
                    record.setTag(tag.getName());
                    break;
            }
        });
    }

    public static TemplateConstraintRecord toRecord(Constraint constraint, int templateId) {
        TemplateConstraintRecord constraintRecord = new TemplateConstraintRecord();
        constraintRecord.setTemplate(templateId);

        return merge(constraintRecord, constraint, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Constraint.NAME_FIELD_NUMBER:
                    record.setConstraint(constraint.getName());
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
                case Template.NAME_FIELD_NUMBER: record.setTitle(template.getName());
                    break;
                case Template.CONTENT_FIELD_NUMBER: record.setTemplate(template.getContent());
                    break;
                case Template.ANSWER_TYPE_FIELD_NUMBER:
                    if (template.getAnswerType() == AnswerType.IMAGE) {
                        record.setAnswerType("image/*");
                    } else {
                        record.setAnswerType(null);
                    }

                    break;
            }
        });
    }
}
