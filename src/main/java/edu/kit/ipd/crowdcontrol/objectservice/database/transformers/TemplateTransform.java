package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;

/**
 * Transforms template protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class TemplateTransform extends AbstractTransform {
    /**
     * Converts a template record to its protobuf representation.
     *
     * @param record template record
     *
     * @return Template.
     */
    public static edu.kit.ipd.crowdcontrol.objectservice.proto.Template toProto(TemplateRecord record) {
        AnswerType answerType = "IMAGE".equals(record.getAnswerType())
                ? AnswerType.IMAGE
                : AnswerType.TEXT;

        return edu.kit.ipd.crowdcontrol.objectservice.proto.Template.newBuilder()
                .setId(record.getIdTemplate())
                .setName(record.getTitel())
                .setContent(record.getTemplate())
                .setAnswerType(answerType).build();
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
                case Template.ANSWER_TYPE_FIELD_NUMBER: record.setAnswerType(template.getAnswerType().name());
                    break;
            }
        });
    }
}
