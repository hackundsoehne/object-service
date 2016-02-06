package edu.kit.ipd.crowdcontrol.objectservice.feedback;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.jooq.Result;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sends feedback to workers according to their answers.
 *
 * @author Felix Rittler
 */
public class FeedbackSender {

    private MailHandler handler;
    private WorkerOperations workerOps;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> schedule = null;

    public FeedbackSender(MailHandler handler, WorkerOperations workerOps) {
        this.handler = handler;
        this.workerOps = workerOps;
    }

    /**
     * Starts the MoneyTransferManager, so giftcodes become submitted to workers every 7 days.
     */
    public synchronized void start() {
        if (schedule != null) {
            throw new IllegalStateException("run() was called twice!");
        }

        schedule = scheduler.scheduleAtFixedRate(this::sendFeedback, 7, 7, TimeUnit.DAYS);

    }

    /**
     * Shuts the MoneyTransferManager down.
     */
    public synchronized void shutdown() {
        schedule.cancel(false);
        scheduler.shutdown();
        schedule = null;
    }

    public void sendFeedback() {
        Result<WorkerRecord> workers = workerOps.getWorkerWithCreditBalanceGreaterOrEqual(0);
        for (WorkerRecord worker : workers) {
            String message = buildFeedbackMessage(worker);
        }
    }

    private String buildFeedbackMessage(WorkerRecord worker) {
        return null;
    }
}
