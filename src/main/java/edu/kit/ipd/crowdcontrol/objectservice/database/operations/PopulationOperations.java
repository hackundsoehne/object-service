package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.transforms.PopulationTransform;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Population;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.*;

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
     * @param cursor Pagination cursor
     * @param next   {@code true} for next, {@code false} for previous
     * @param limit  Number of records
     *
     * @return List of populations
     */
    public Range<Population, Integer> getPopulationFrom(int cursor, boolean next, int limit) {
        // Join is more complicated and the performance gain would be negligible considering the the
        // expected moderate usage
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
     * @param id ID of the population
     *
     * @return The population
     */
    public Optional<Population> getPopulation(int id) {
        // Join is more complicated and the performance gain would be negligible considering the the
        // expected moderate usage
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
     * @param toStore Population to save
     *
     * @return Population with ID assigned
     *
     * @throws IllegalArgumentException if the name or content is not set
     */
    public Population insertPopulation(Population toStore) throws IllegalArgumentException {
        assertHasField(toStore,
                Population.NAME_FIELD_NUMBER,
                Population.QUESTION_FIELD_NUMBER,
                Population.ANSWERS_FIELD_NUMBER);

        PopulationRecord population = PopulationTransform.mergeRecord(create.newRecord(POPULATION), toStore);
        population.store();

        toStore.getAnswersList().stream()
                .map(s -> new PopulationAnswerOptionRecord(null, population.getIdPopulation(), s))
                .collect(Collectors.collectingAndThen(Collectors.toList(), create::batchInsert))
                .execute();

        List<PopulationAnswerOptionRecord> answers = create.selectFrom(POPULATION_ANSWER_OPTION)
                .where(POPULATION_ANSWER_OPTION.POPULATION.eq(population.getIdPopulation()))
                .fetch();

        return PopulationTransform.toProto(population, answers);
    }

    /**
     * Deletes a population.
     *
     * @param id ID of the population
     *
     * @return {@code true} if deleted, {@code false} otherwise
     * @throws IllegalArgumentException if the population is still in use
     */
    public boolean deletePopulation(int id) throws IllegalArgumentException{
        boolean isUsed = create.fetchExists(
                DSL.select()
                    .from(EXPERIMENTSPOPULATION)
                    .join(POPULATION_ANSWER_OPTION).onKey()
                    .where(POPULATION_ANSWER_OPTION.POPULATION.eq(id))
        );

        if (isUsed) {
            throw new IllegalArgumentException(String.format("Population %d is still in used", id));
        }

        return create.deleteFrom(POPULATION)
                .where(POPULATION.ID_POPULATION.eq(id))
                .execute() == 1;
    }
}
