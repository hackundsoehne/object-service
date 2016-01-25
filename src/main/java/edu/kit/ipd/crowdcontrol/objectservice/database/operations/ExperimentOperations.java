package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Optional;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.TEMPLATE;

/**
 * responsible for the operations on the Experiment-Table
 * @author LeanderK
 * @version 1.0
 */
public class ExperimentOperations extends AbstractOperations {
    public ExperimentOperations(DSLContext create) {
        super(create);
    }

    //TODO need the protobuf for "real" methods
    /**
     * inserts the Experiment into the database
     * @param experimentRecord the record to insert
     * @return the resulting id of the experiment
     */
    public int insertNewExperiment(ExperimentRecord experimentRecord) {
        return create.executeInsert(experimentRecord);
    }

    /**
     * returns the experiment corresponding to the id
     * @param id the id
     * @return the optional experiment
     */
    public Optional<ExperimentRecord> getExperiment(int id) {
        return create.selectFrom(Tables.EXPERIMENT)
                .where(Tables.EXPERIMENT.ID_EXPERIMENT.eq(id))
                .fetchOptional();
    }

    /**
     * updates the experiment and returns whether it was successful
     * @param experimentRecord the record to update
     * @return true if successful, false if not
     */
    public boolean updateExperiment(ExperimentRecord experimentRecord) {
        return doIfNotRunning(experimentRecord.getIdExperiment(), trans ->
                DSL.using(trans).executeUpdate(experimentRecord) == 1);
    }

    /**
     * deletes the experiment with the passed id if it is not running.
     * @param id the id of the experiment to delete
     * @return true if deleted, false if not
     * @throws IllegalArgumentException if the experiment is running
     */
    public boolean deleteExperiment(int id) throws IllegalArgumentException {
        return doIfNotRunning(id, trans -> {
            int deleted = DSL.using(trans)
                    .deleteFrom(Tables.EXPERIMENT)
                    .where(Tables.EXPERIMENT.ID_EXPERIMENT.eq(id))
                    .execute();
            return deleted == 1;
        });
    }

    public Range<Experiment, Integer> getExperimentsFrom(int cursor, boolean next, int limit) {
        //TODO implement
        return null;
    }

    public Experiment toProto(ExperimentRecord record) {
        return null;
    }

    public ExperimentRecord toRecord(Experiment experiment) {
        return null;
    }
}
