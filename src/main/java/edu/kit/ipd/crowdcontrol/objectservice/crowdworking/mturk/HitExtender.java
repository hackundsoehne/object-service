package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by marcel on 17.03.16.
 */
public class HitExtender extends TimerTask {
    private final int MINIMUM_TIME_DAYS = 3;
    private final int INTERVAL_RUN_HOURS = 24;
    private final List<String> hitIds;

    public HitExtender(List<String> hitIds) {
        this.hitIds = new ArrayList<>();
        this.hitIds.addAll(hitIds);
        Timer timer = new Timer();

        timer.schedule(this, 0, TimeUnit.MILLISECONDS.toHours(INTERVAL_RUN_HOURS));
    }

    public void addHit(String id) {
        synchronized (hitIds) {
            hitIds.remove(id);
        }
    }

    public void removeHit(String id) {
        synchronized (hitIds) {
            hitIds.remove(id);
        }
    }

    @Override
    public void run() {
        //TODO extend the hit
    }
}
