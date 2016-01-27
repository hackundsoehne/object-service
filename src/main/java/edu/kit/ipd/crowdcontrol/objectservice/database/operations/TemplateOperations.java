package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.TemplateTransform;
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
    public Range<Template, Integer> getTemplatesFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(TEMPLATE), TEMPLATE.ID_TEMPLATE, cursor, next, limit)
                .map(TemplateTransform::toProto);
    }

    /**
     * Returns a single template.
     *
     * @param id
     *         ID of the template.
     *
     * @return The template.
     */
    public Optional<Template> getTemplate(int id) {
        return create.fetchOptional(TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id))
                .map(TemplateTransform::toProto);
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
    public Template insertTemplate(Template toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Template.NAME_FIELD_NUMBER,
                Template.CONTENT_FIELD_NUMBER);

        TemplateRecord record = TemplateTransform.mergeRecord(create.newRecord(TEMPLATE), toStore);
        record.store();

        return TemplateTransform.toProto(record);
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
    public Template updateTemplate(int id, Template template) {
        TemplateRecord record = create
                .fetchOptional(TEMPLATE, TEMPLATE.ID_TEMPLATE.eq(id))
                .orElseThrow(() -> new NotFoundException("Template does not exist!"));

        record = TemplateTransform.mergeRecord(record, template);
        record.update();

        return TemplateTransform.toProto(record);
    }

    /**
     * Deletes a template.
     *
     * @param id
     *         ID of the template.
     *
     * @return {@code true} if deleted, {@code false} otherwise.
     */
    public boolean deleteTemplate(int id) {
        TemplateRecord record = create.newRecord(Tables.TEMPLATE);
        record.setIdTemplate(id);

        return create.executeDelete(record, Tables.TEMPLATE.ID_TEMPLATE.eq(id)) == 1;
    }
}
