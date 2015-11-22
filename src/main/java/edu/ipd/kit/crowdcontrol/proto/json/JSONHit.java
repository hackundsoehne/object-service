package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord;

/**
 * This class does not represent the HIT stored in the Database. It represents the JSON used to communicate between Client and Server.
 * @author LeanderK
 * @version 1.0
 */
public class JSONHit {
    private final String experimentTitel;
    @JSONRequired
    private final int idHit;
    private final int experimentID;
    private final String type;
    private final int current_amount;
    private final int max_amount;
    private final int payment;
    private final int bonus;

    public JSONHit(String experimentTitel, HitRecord hitRecord) {
        this.experimentTitel = experimentTitel;
        idHit = hitRecord.getIdhit();
        experimentID = hitRecord.getExperimentH();
        type = hitRecord.getType();
        current_amount = hitRecord.getCurrentAmount();
        max_amount = hitRecord.getMaxAmount();
        payment = hitRecord.getPayment();
        bonus = hitRecord.getBonus();
    }

    public JSONHit(String experimentTitel, Hit hit) {
        this.experimentTitel = experimentTitel;
        idHit = hit.getIdhit();
        experimentID = hit.getExperimentH();
        type = hit.getType();
        current_amount = hit.getCurrentAmount();
        max_amount = hit.getMaxAmount();
        payment = hit.getPayment();
        bonus = hit.getBonus();
    }

    public HitRecord getRecord() {
        return new HitRecord(idHit, null, type, null, null, max_amount, payment, bonus, null, null);
    }

    public int getIdHit() {
        return idHit;
    }
}
