package edu.kit.ipd.crowdcontrol.objectservice.database;

import org.jooq.DSLContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static edu.kit.ipd.crowdcontrol.objectservice.database.model.Tables.RATING;

/**
 * this class periodically performs cleanup-tasks.
 * @author LeanderK
 * @version 1.0
 */
public class DatabaseMaintainer {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DSLContext create;
    private final int cleanUpInterval;
    private ScheduledFuture<?> schedule = null;

    /**
     * creates a new Instance of DatabaseMaintainer.
     * @param create the context used to communicate with the database
     * @param cleanUpInterval the intervall the task should be run, in hours
     */
    public DatabaseMaintainer(DSLContext create, int cleanUpInterval) {
        this.create = create;
        this.cleanUpInterval = cleanUpInterval;
    }

    /**
     * starts the DatabaseMaintainer
     */
    public synchronized void start() {
        if (schedule != null) {
            throw new IllegalStateException("run() was called twice!");
        }

        schedule = scheduler.scheduleAtFixedRate(this::doCleanUp, 0, cleanUpInterval, TimeUnit.HOURS);
    }

    /**
     * deletes all the unused rating.
     * A Rating is unused when after 2 hours the worker didn't provide the rating.
     */
    private void doCleanUp() {
        LocalDateTime limit = LocalDateTime.now().minus(2, ChronoUnit.HOURS);
        Timestamp timestamp = Timestamp.valueOf(limit);

        create.deleteFrom(RATING)
                .where(RATING.RATING_.isNull())
                .and(RATING.TIMESTAMP.lessThan(timestamp))
                .execute();
    }

    /**
     * shuts the DatabaseMaintainer down.
     */
    public void shutdown() {
        schedule.cancel(false);
        scheduler.shutdown();
    }
}
