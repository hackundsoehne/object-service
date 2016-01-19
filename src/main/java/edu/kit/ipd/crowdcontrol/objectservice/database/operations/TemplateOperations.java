package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.rest.BadRequestException;
import edu.kit.ipd.crowdcontrol.objectservice.rest.NotFoundException;
import org.jooq.DSLContext;
import org.jooq.Result;

/**
 * @author Niklas Keller
 */
public class TemplateOperations extends AbstractOperations {
    public TemplateOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of templates starting from {@code cursor}.
     *
     * @param cursor
     *         Pagination cursor.
     * @param next
     *         {@code true} for next, {@code false} for previous.
     * @param limit
     *         Number of records.
     *
     * @return List of templates.
     */
    public Range<Template, Integer> all(int cursor, boolean next, int limit) {
        Range<TemplateRecord, Integer> range = getNextRange(
                create.selectFrom(Tables.TEMPLATE),
                Tables.TEMPLATE.ID_TEMPLATE, cursor, next, limit
        );

        return range.map(this::toProto);
    }

    /**
     * Returns a single template.
     *
     * @param id
     *         ID of the template.
     *
     * @return The template.
     */
    public Template get(int id) {
        Result<TemplateRecord> result = create.fetch(Tables.TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id));

        if (result.isEmpty()) {
            return null;
        }

        return toProto(result.get(0));
    }

    /**
     * Creates a new template.
     *
     * @param template
     *         Template to save.
     *
     * @return Template with ID assigned.
     */
    public Template create(Template template) {
        if (!hasFields(template, Template.NAME_FIELD_NUMBER, Template.CONTENT_FIELD_NUMBER)) {
            throw new BadRequestException("Name and content must be set!");
        }

        String type = template.getAnswerType().name();

        TemplateRecord record = create.newRecord(Tables.TEMPLATE);
        record.setTitel(template.getName());
        record.setTemplate(template.getContent());
        record.setAnswerType(type.equals("INVALID") ? "TEXT" : type);
        record.store();

        return this.get(record.getIdTemplate());
    }

    /**
     * Updates a template.
     *
     * @param id
     *         ID of the template.
     * @param template
     *         New template contents.
     *
     * @return Updated template.
     */
    public Template update(int id, Template template) {
        TemplateRecord record = create.fetchOne(Tables.TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id));

        if (record == null) {
            throw new NotFoundException("Template does not exist!");
        }

        if (hasFields(template, Template.NAME_FIELD_NUMBER)) {
            record.setTitel(template.getName());
        }

        if (hasFields(template, Template.CONTENT_FIELD_NUMBER)) {
            record.setTemplate(template.getContent());
        }

        if (template.getAnswerType() != AnswerType.INVALID) {
            record.setAnswerType(template.getAnswerType().name());
        }

        if (record.changed()) {
            record.store();
        }

        return this.get(id);
    }

    /**
     * Deletes a template.
     *
     * @param id
     *         ID of the template.
     */
    public void delete(int id) {
        TemplateRecord record = create.fetchOne(Tables.TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id));

        if (record == null) {
            throw new NotFoundException("Template does not exist!");
        }

        record.delete();
    }

    private static boolean hasFields(Template template, int... fields) {
        for (int field : fields) {
            if (!template.hasField(Template.getDescriptor().findFieldByNumber(field))) {
                return false;
            }
        }

        return true;
    }

    private Template toProto(TemplateRecord record) {
        AnswerType answerType = "IMAGE".equals(record.getAnswerType())
                ? AnswerType.IMAGE
                : AnswerType.TEXT;

        return Template.newBuilder()
                .setId(record.getIdTemplate())
                .setName(record.getTitel())
                .setContent(record.getTemplate())
                .setAnswerType(answerType).build();
    }
}
