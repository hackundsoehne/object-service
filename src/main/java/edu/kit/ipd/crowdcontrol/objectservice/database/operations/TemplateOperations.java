package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionTemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateConstraintRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateTagRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.TemplateTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Constraint;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Tag;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

/**
 * responsible for the operations concerning the Template and Rating_Option_Template tables.
 *
 * @author Niklas Keller
 */
public class TemplateOperations extends AbstractOperations {
    public TemplateOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of templates starting from {@code cursor}.
     *
     * @param cursor Pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     *
     * @return List of templates
     */
    public Range<Template, Integer> getTemplatesFrom(int cursor, boolean next, int limit) {
        // TODO: Options, Tags and Constraints
        return getNextRange(create.selectFrom(TEMPLATE), TEMPLATE.ID_TEMPLATE, TEMPLATE, cursor, next, limit)
                .map(TemplateTransformer::toProto);
    }

    /**
     * Returns a single template.
     *
     * @param id the ID of the template
     *
     * @return The template
     */
    public Optional<Template> getTemplate(int id) {
        return create.fetchOptional(TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id))
                .map(template -> {
                    Result<RatingOptionTemplateRecord> options = create.selectFrom(RATING_OPTION_TEMPLATE)
                            .where(RATING_OPTION_TEMPLATE.TEMPLATE.eq(id))
                            .fetch();

                    Result<TemplateTagRecord> tags = create.selectFrom(TEMPLATE_TAG)
                            .where(TEMPLATE_TAG.TEMPLATE.eq(id))
                            .fetch();

                    Result<TemplateConstraintRecord> constraints = create.selectFrom(TEMPLATE_CONSTRAINT)
                            .where(TEMPLATE_CONSTRAINT.TEMPLATE.eq(id))
                            .fetch();

                    return TemplateTransformer.toProto(template, options, tags, constraints);
                });
    }

    /**
     * Creates a new template.
     *
     * @param toStore the Template to save
     *
     * @return Template with ID assigned
     *
     * @throws IllegalArgumentException if the name or content is not set
     */
    public Template insertTemplate(Template toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Template.NAME_FIELD_NUMBER,
                Template.CONTENT_FIELD_NUMBER);

        TemplateRecord record = TemplateTransformer.mergeRecord(create.newRecord(TEMPLATE), toStore);
        record.store();

        List<RatingOptionTemplateRecord> options = toStore.getRatingOptionsList().stream()
                .map(option -> TemplateTransformer.toRecord(option, record.getIdTemplate()))
                .collect(Collectors.toList());

        create.batchInsert(options).execute();

        List<TemplateTagRecord> tags = toStore.getTagsList().stream()
                .filter(tag -> !tag.getName().isEmpty())
                .map(tag -> TemplateTransformer.toRecord((Tag) tag, record.getIdTemplate()))
                .collect(Collectors.toList());

        create.batchInsert(tags).execute();

        List<TemplateConstraintRecord> constraints = toStore.getConstraintsList().stream()
                .filter(constraint -> !constraint.getName().isEmpty())
                .map(constraint -> TemplateTransformer.toRecord((Constraint) constraint, record.getIdTemplate()))
                .collect(Collectors.toList());

        create.batchInsert(constraints).execute();

        return getTemplate(record.getIdTemplate())
                .orElseThrow(() -> new IllegalStateException("inserted Template is absent"));
    }

    /**
     * Updates a template
     *
     * @param id       the ID of the template
     * @param template the new template contents
     *
     * @return the updated template
     */
    public Template updateTemplate(int id, Template template) {
        TemplateRecord templateRecord = create
                .fetchOptional(TEMPLATE, TEMPLATE.ID_TEMPLATE.eq(id))
                .orElseThrow(() -> new NotFoundException("Template does not exist!"));

        templateRecord = TemplateTransformer.mergeRecord(templateRecord, template);

        if (!template.getRatingOptionsList().isEmpty()) {
            List<RatingOptionTemplateRecord> toInsert = template.getRatingOptionsList().stream()
                    .map(option -> TemplateTransformer.toRecord(option, id))
                    .collect(Collectors.toList());

            create.transaction(conf -> {
                DSL.using(conf).deleteFrom(RATING_OPTION_TEMPLATE)
                        .where(RATING_OPTION_TEMPLATE.TEMPLATE.eq(id))
                        .execute();

                DSL.using(conf).batchInsert(toInsert).execute();
            });
        }

        if (!template.getTagsList().isEmpty()) {
            List<TemplateTagRecord> toInsert = template.getTagsList().stream()
                    .filter(tag -> !tag.getName().isEmpty())
                    .map(tag -> TemplateTransformer.toRecord((Tag) tag, id))
                    .collect(Collectors.toList());

            create.transaction(conf -> {
                DSL.using(conf).deleteFrom(TEMPLATE_TAG)
                        .where(TEMPLATE_TAG.TEMPLATE.eq(id))
                        .execute();

                DSL.using(conf).batchInsert(toInsert).execute();
            });
        }

        if (!template.getConstraintsList().isEmpty()) {
            List<TemplateConstraintRecord> toInsert = template.getConstraintsList().stream()
                    .filter(constraint -> !constraint.getName().isEmpty())
                    .map(constraint -> TemplateTransformer.toRecord((Constraint) constraint, id))
                    .collect(Collectors.toList());

            create.transaction(conf -> {
                DSL.using(conf).deleteFrom(TEMPLATE_TAG)
                        .where(TEMPLATE_TAG.TEMPLATE.eq(id))
                        .execute();

                DSL.using(conf).batchInsert(toInsert).execute();
            });
        }

        templateRecord.update();

        return TemplateTransformer.toProto(templateRecord);
    }

    /**
     * Deletes a template.
     *
     * @param id the ID of the template
     *
     * @return {@code true} if deleted, {@code false} otherwise
     */
    public boolean deleteTemplate(int id) {
        TemplateRecord record = create.newRecord(Tables.TEMPLATE);
        record.setIdTemplate(id);

        return create.executeDelete(record, Tables.TEMPLATE.ID_TEMPLATE.eq(id)) == 1;
    }
}
