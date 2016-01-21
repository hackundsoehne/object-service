package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.TEMPLATE;

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
        return getNextRange(create.selectFrom(TEMPLATE), TEMPLATE.ID_TEMPLATE, cursor, next, limit)
                .map(this::toProto);
    }

    /**
     * Returns a single template.
     *
     * @param id
     *         ID of the template.
     *
     * @return The template.
     */
    public Optional<Template> get(int id) {
        return create.fetchOptional(TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id))
                .map(this::toProto);
    }

    /**
     * Creates a new template.
     *
     * @param toStore
     *         Template to save.
     *
     * @return Template with ID assigned.
     * @throws IllegalArgumentException if the name or content is not set
     */
    public Template create(Template toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Template.NAME_FIELD_NUMBER,
                Template.CONTENT_FIELD_NUMBER);

        TemplateRecord record = mergeRecord(create.newRecord(TEMPLATE), toStore);
        record.store();

        return toProto(record);
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
        TemplateRecord record = create
                .fetchOptional(TEMPLATE, TEMPLATE.ID_TEMPLATE.eq(id))
                .orElseThrow(() -> new NotFoundException("Template does not exist!"));

        record = mergeRecord(record, template);
        record.update();

        return toProto(record);
    }

    /**
     * Deletes a template.
     *
     * @param id
     *         ID of the template.
     *
     * @return {@code true} if deleted, {@code false} otherwise.
     */
    public boolean delete(int id) {
        TemplateRecord record = create.newRecord(Tables.TEMPLATE);
        record.setIdTemplate(id);

        return create.executeDelete(record, Tables.TEMPLATE.ID_TEMPLATE.eq(id)) == 1;
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

    private TemplateRecord mergeRecord(TemplateRecord target, Template template) {
        if (template.hasField(template.getDescriptorForType().findFieldByNumber(Template.NAME_FIELD_NUMBER))) {
            target.setTitel(template.getName());
        }

        if (template.hasField(template.getDescriptorForType().findFieldByNumber(Template.CONTENT_FIELD_NUMBER))) {
            target.setTemplate(template.getContent());
        }

        if (template.hasField(template.getDescriptorForType().findFieldByNumber(Template.ANSWER_TYPE_FIELD_NUMBER))) {
            target.setAnswerType(template.getAnswerType().name());
        }

        return target;
    }
}
