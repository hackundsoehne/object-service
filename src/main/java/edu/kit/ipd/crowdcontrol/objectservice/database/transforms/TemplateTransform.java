package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;

/**
 * Transforms template protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class TemplateTransform {
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
    public static TemplateRecord mergeRecord(TemplateRecord target, edu.kit.ipd.crowdcontrol.objectservice.proto.Template template) {
        if (template.hasField(template.getDescriptorForType().findFieldByNumber(edu.kit.ipd.crowdcontrol.objectservice.proto.Template.NAME_FIELD_NUMBER))) {
            target.setTitel(template.getName());
        }

        if (template.hasField(template.getDescriptorForType().findFieldByNumber(edu.kit.ipd.crowdcontrol.objectservice.proto.Template.CONTENT_FIELD_NUMBER))) {
            target.setTemplate(template.getContent());
        }

        if (template.hasField(template.getDescriptorForType().findFieldByNumber(edu.kit.ipd.crowdcontrol.objectservice.proto.Template.ANSWER_TYPE_FIELD_NUMBER))) {
            target.setAnswerType(template.getAnswerType().name());
        }

        return target;
    }
}
