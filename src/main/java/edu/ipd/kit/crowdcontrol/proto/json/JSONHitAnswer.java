package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord;

/**
 * This class does not represent the HIT stored in the Database. It models an Answer where a Client requests information about a Hit.
 * @author LeanderK
 * @version 1.0
 */
public class JSONHitAnswer {
    private final String experimentTitel;
    private final int idHit;
    private final int experimentID;
    private final String type;
    private final int current_amount;
    private final int max_amount;
    private final int payment;
    private final int bonus;

    public JSONHitAnswer(String experimentTitel, HitRecord hitRecord) {
        this.experimentTitel = experimentTitel;
        idHit = hitRecord.getIdhit();
        experimentID = hitRecord.getExperimentH();
        type = hitRecord.getType();
        current_amount = hitRecord.getCurrentAmount();
        max_amount = hitRecord.getMaxAmount();
        payment = hitRecord.getPayment();
        bonus = hitRecord.getBonus();
    }
}
