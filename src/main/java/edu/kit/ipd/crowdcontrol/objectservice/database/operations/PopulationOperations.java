package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.PopulationTransform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Population;
import org.jooq.DSLContext;
import org.jooq.impl.UpdatableRecordImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.POPULATION;
import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.POPULATION_ANSWER_OPTION;

/**
 * @author Niklas Keller
 */
public class PopulationOperations extends AbstractOperations {
    /**
     * Creates a new population operations instance.
     *
     * @param create context used to communicate with the database
     */
    public PopulationOperations(DSLContext create) {
        super(create);
    }

    /**
     * Returns a range of populations starting from {@code cursor}.
     *
     * @param cursor Pagination cursor.
     * @param next   {@code true} for next, {@code false} for previous.
     * @param limit  Number of records.
     *
     * @return List of populations.
     */
    public Range<Population, Integer> getPopulationList(int cursor, boolean next, int limit) {
        // TODO: @Leander: Refactor to use JOIN?

        return getNextRange(create.selectFrom(POPULATION), POPULATION.ID_POPULATION, cursor, next, limit)
                .map(populationRecord -> {
                    List<PopulationAnswerOptionRecord> answers = create.selectFrom(POPULATION_ANSWER_OPTION)
                            .where(POPULATION_ANSWER_OPTION.POPULATION.eq(populationRecord.getIdPopulation()))
                            .fetch();

                    return PopulationTransform.toProto(populationRecord, answers);
                });
    }

    /**
     * Returns a single population.
     *
     * @param id ID of the population.
     *
     * @return The population.
     */
    public Optional<Population> getPopulation(int id) {
        // TODO: @Leander: Refactor to use JOIN?

        return create.fetchOptional(POPULATION, Tables.POPULATION.ID_POPULATION.eq(id))
                .map(populationRecord -> {
                    List<PopulationAnswerOptionRecord> answers = create.selectFrom(POPULATION_ANSWER_OPTION)
                            .where(POPULATION_ANSWER_OPTION.POPULATION.eq(populationRecord.getIdPopulation()))
                            .fetch();

                    return PopulationTransform.toProto(populationRecord, answers);
                });
    }

    /**
     * Creates a new population.
     *
     * @param toStore Population to save.
     *
     * @return Population with ID assigned.
     *
     * @throws IllegalArgumentException if the name or content is not set
     */
    public Population createPopulation(Population toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Population.QUESTION_FIELD_NUMBER,
                Population.ANSWERS_FIELD_NUMBER);

        // TODO: @Leander: Update database operation to be performant and correct.

        PopulationRecord record = PopulationTransform.mergeRecord(create.newRecord(POPULATION), toStore);
        record.store();

        List<PopulationAnswerOptionRecord> answerRecords = toStore.getAnswersList().stream()
                .map(s -> {
                    PopulationAnswerOptionRecord rec = create.newRecord(POPULATION_ANSWER_OPTION);
                    rec.setAnswer(s);
                    return rec;
                })
                .collect(Collectors.toList());

        answerRecords.stream().forEach(UpdatableRecordImpl::store);

        return PopulationTransform.toProto(record, answerRecords);
    }

    /**
     * Deletes a population.
     *
     * @param id ID of the population.
     *
     * @return {@code true} if deleted, {@code false} otherwise.
     */
    public boolean delete(int id) {
        // TODO: @Leander: Check if currently used in an experiment?

        PopulationRecord record = create.newRecord(Tables.POPULATION);
        record.setIdPopulation(id);

        return create.executeDelete(record, Tables.POPULATION.ID_POPULATION.eq(id)) == 1;
    }
}
