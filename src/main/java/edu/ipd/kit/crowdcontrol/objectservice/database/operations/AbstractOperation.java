package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.Tables;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.function.Function;

/**
 * @author LeanderK
 * @version 1.0
 */
public abstract class AbstractOperation {
    protected final DSLContext create;

    protected AbstractOperation(DSLContext create) {
        this.create = create;
    }

    /**
     * executes the function if the experiment is not running.
     * @param id the id of the event
     * @param function the function to execute
     * @param <R> the return type
     * @return the result of the function
     */
    protected <R> R doIfNotRunning(int id, Function<Configuration, R> function) {
        return create.transactionResult(trans -> {
            int running = DSL.using(trans)
                    .fetchCount(
                            Tables.TASK,
                            Tables.TASK.EXPERIMENT.eq(id).and(Tables.TASK.RUNNING.isTrue()));
            if (running == 0) {
                return function.apply(trans);
            } else {
                //TODO other exception?
                throw new IllegalArgumentException("Experiment is running");
            }
        });
    }
}
