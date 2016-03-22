package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;


import com.amazonaws.mturk.requester.doc._2014_08_15.HIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.ExtendHIT;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command.GetHIT;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Class which extends all Hits added to the object.
 *
 * They are getting extended all 24h to be in the future more than 30 days.
 *
 * The object owns a internal list of hit´s which are updated all 24h.
 *
 * @author Marcel Hollerbach
 */
public class HitExtender extends TimerTask {
    private static final Logger LOGGER = LogManager.getLogger(HitExtender.class);
    private final int MINIMUM_TIME_DAYS = 3;
    private final int INTERVAL_RUN_HOURS = 24;
    private final MTurkConnection connection;
    private final List<String> hitIds;

    /**
     * Create a new Extender
     * @param hitIds list of hits which is known to the system at creation time
     * @param connection the connection to use
     */
    public HitExtender(List<String> hitIds, MTurkConnection connection) {
        this.connection = connection;
        this.hitIds = new ArrayList<>();
        this.hitIds.addAll(hitIds);
        Timer timer = new Timer();

        long l = TimeUnit.MILLISECONDS.convert(INTERVAL_RUN_HOURS, TimeUnit.HOURS);
        timer.schedule(this, new Date(), l);
    }

    /**
     * Add a new hit to the list of hits
     *
     * @param id hit id to add
     */
    public void addHit(String id) {
        synchronized (hitIds) {
            hitIds.add(id);
        }
    }

    /**
     * Remove a hit from the list of hits
     *
     * After this call the hit will not be updated anymore.
     *
     * @param id hit id to remove
     */
    public void removeHit(String id) {
        synchronized (hitIds) {
            hitIds.remove(id);
        }
    }

    @Override
    public void run() {
        List<String> workCopy;

        LOGGER.trace("Starting updating hits");

        synchronized (hitIds) {
            workCopy = new ArrayList<>();
            workCopy.addAll(hitIds);
        }

        Function<HIT, CompletableFuture<Boolean>> hitExtend = (hit -> {
            LocalDate expire = hit.getExpiration().toGregorianCalendar().toZonedDateTime().toLocalDate();
            LocalDate minExpire = LocalDate.now().plusDays(MINIMUM_TIME_DAYS);
            long days = ChronoUnit.DAYS.between(expire, minExpire);
            if (days > 0) {
                LOGGER.trace("Update "+hit.getHITId()+" with up to "+days+" minutes");
                return new ExtendHIT(connection, hit.getHITId(), 0, Duration.ofDays(2));
            }
            return CompletableFuture.completedFuture(true);
        });


        if (!workCopy.stream()
                .map(id -> new GetHIT(connection, id))
                .map(CompletableFuture::join)
                .map(hitExtend)
                .map(CompletableFuture::join)
                .allMatch(Boolean::booleanValue)) {
            LOGGER.log(Level.ERROR, "Failed to extend hits");
        }

        LOGGER.trace("Finished updating hits");
    }
}
