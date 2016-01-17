package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.AnswerType;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.proto.TemplateList;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;

/**
 * @author Niklas Keller
 */
public class TemplateOperation extends AbstractOperation {
    public TemplateOperation(DSLContext create) {
        super(create);
    }

    // TODO: DB calls

    /**
     * Returns 20 templates starting from {@code ref}.
     *
     * @param ref
     *         ID of the first element.
     * @param asc
     *         Larger or smaller IDs?
     *
     * @return List of templates.
     */
    public TemplateList all(int ref, boolean asc) {
        // TODO: Use boolean or enum for ASC / DESC?
        // TODO: How to do next and prev for pagination? Select one more + the one before?
        Condition condition = asc
                ? Tables.TEMPLATE.IDTEMPLATE.greaterOrEqual(ref)
                : Tables.TEMPLATE.IDTEMPLATE.lessOrEqual(ref);

        Result<TemplateRecord> result = create.selectFrom(Tables.TEMPLATE)
                .where(condition)
                .limit(20)
                .fetch();

        TemplateList.Builder builder = TemplateList.newBuilder();

        for (TemplateRecord record : result) {
            builder.addItems(toProto(record));
        }

        return builder.build();
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
        Result<TemplateRecord> result = create.selectFrom(Tables.TEMPLATE)
                .where(Tables.TEMPLATE.IDTEMPLATE.eq(id))
                .fetch();

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
        // TODO: Use passed template or call .get(id)?
        return template.toBuilder().setId(1).build();
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
        // TODO: Use template.getId or pass id?
        // TODO: Use passed template or call .get(id)?
        return template;
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
        // TODO: Void + exception or boolean?
        return false;
    }

    private Template toProto(TemplateRecord record) {
        AnswerType answerType = record.getAnswerType().equals("IMAGE")
                ? AnswerType.IMAGE
                : AnswerType.TEXT;

        return Template.newBuilder()
                .setId(record.getIdtemplate())
                .setName(record.getTitel())
                .setContent(record.getTemplate())
                .setAnswerType(answerType).build();
    }
}
