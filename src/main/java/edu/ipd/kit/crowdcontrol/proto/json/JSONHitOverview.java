package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JSONHitOverview {
    private final int id;
    private final String type;
    private final boolean running;

    public JSONHitOverview(int id, String type, boolean running) {
        this.id = id;
        this.type = type;
        this.running = running;
    }

    public JSONHitOverview(Hit hit) {
        this.id = hit.getIdhit();
        this.type = hit.getType();
        this.running = hit.getRunning();
    }
}
