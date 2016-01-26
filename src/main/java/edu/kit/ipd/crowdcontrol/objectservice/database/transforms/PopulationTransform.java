package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationAnswerOptionRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PopulationRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Population;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms population protocol buffers to database records.
 *
 * @author Niklas Keller
 */
public class PopulationTransform extends AbstractTransform {
    /**
     * Converts a population record to its protobuf representation.
     *
     * @param record population record
     *
     * @return Population.
     */
    public static Population toProto(PopulationRecord record, List<PopulationAnswerOptionRecord> options) {
        return Population.newBuilder()
                .setId(record.getIdPopulation())
                .setQuestion(record.getName())
                .addAllAnswers(options.stream().map(PopulationAnswerOptionRecord::getAnswer).collect(Collectors.toList()))
                .build();
    }

    /**
     * Merges a record with the set properties of a protobuf population.
     *
     * @param target record to merge into
     * @param population message to merge from
     *
     * @return Merged population record.
     */
    public static PopulationRecord mergeRecord(PopulationRecord target, Population population) {
        return merge(target, population, (fieldNumber, record) -> {
            switch (fieldNumber) {
                case Population.QUESTION_FIELD_NUMBER:
                    record.setName(population.getQuestion());
                    break;
            }
        });
    }
}
