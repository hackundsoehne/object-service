package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionTemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.TemplateTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.rest.exceptions.NotFoundException;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.RATING_OPTION_TEMPLATE;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.TEMPLATE;

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
     * @param next {@code true} for next, {@code false} for previous
     * @param limit Number of records
     * @return List of templates
     */
    public Range<Template, Integer> getTemplatesFrom(int cursor, boolean next, int limit) {
        return getNextRange(create.selectFrom(TEMPLATE), TEMPLATE.ID_TEMPLATE, TEMPLATE, cursor, next, limit)
                .map(TemplateTransformer::toProto);
    }

    /**
     * Returns a single template.
     *
     * @param id the ID of the template
     * @return The template
     */
    public Optional<Template> getTemplate(int id) {

        return create.fetchOptional(TEMPLATE, Tables.TEMPLATE.ID_TEMPLATE.eq(id))
                .map(template -> {
                    Result<RatingOptionTemplateRecord> options = create.selectFrom(RATING_OPTION_TEMPLATE)
                            .where(RATING_OPTION_TEMPLATE.TEMPLATE.eq(id))
                            .fetch();
                    return TemplateTransformer.toProto(template, options);
                });
    }

    /**
     * Creates a new template.
     *
     * @param toStore the Template to save
     * @return Template with ID assigned
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
                .peek(option -> option.setIdRatingOptionsTemplate(null))
                .collect(Collectors.toList());

        create.batchInsert(options).execute();

        return getTemplate(record.getIdTemplate())
                .orElseThrow(() -> new IllegalStateException("inserted Template is absent"));
    }

    /**
     * Updates a template
     *
     * @param id the ID of the template
     * @param template the new template contents
     * @return the updated template
     */
    public Template updateTemplate(int id, Template template) {
        TemplateRecord templateRecord = create
                .fetchOptional(TEMPLATE, TEMPLATE.ID_TEMPLATE.eq(id))
                .orElseThrow(() -> new NotFoundException("Template does not exist!"));

        templateRecord = TemplateTransformer.mergeRecord(templateRecord, template);

        if (!template.getRatingOptionsList().isEmpty()) {
            Predicate<Template.RatingOption> hasId = option ->
                    option.hasField(option.getDescriptorForType().findFieldByNumber
                            (Template.RatingOption.TEMPLATE_RATING_ID_FIELD_NUMBER));
            Map<Integer, RatingOptionTemplateRecord> toUpdate = template.getRatingOptionsList().stream()
                    .filter(hasId)
                    .collect(Collectors.toMap(
                            Template.RatingOption::getTemplateRatingId,
                            option -> TemplateTransformer.toRecord(option, id)
                    ));

            List<RatingOptionTemplateRecord> toInsert = template.getRatingOptionsList().stream()
                    .filter(option -> !hasId.test(option))
                    .map(option -> TemplateTransformer.toRecord(option, id))
                    .collect(Collectors.toList());

            create.transaction(conf -> {
                DSL.using(conf).deleteFrom(RATING_OPTION_TEMPLATE)
                        .where(RATING_OPTION_TEMPLATE.TEMPLATE.eq(id))
                        .and(RATING_OPTION_TEMPLATE.ID_RATING_OPTIONS_TEMPLATE.notIn(toUpdate.keySet()))
                        .execute();

                DSL.using(conf).batchUpdate(toUpdate.values()).execute();

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
     * @return {@code true} if deleted, {@code false} otherwise
     */
    public boolean deleteTemplate(int id) {
        TemplateRecord record = create.newRecord(Tables.TEMPLATE);
        record.setIdTemplate(id);

        create.deleteFrom(RATING_OPTION_TEMPLATE)
                .where(RATING_OPTION_TEMPLATE.TEMPLATE.eq(id))
                .execute();

        return create.executeDelete(record, Tables.TEMPLATE.ID_TEMPLATE.eq(id)) == 1;
    }
}
