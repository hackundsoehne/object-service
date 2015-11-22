package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Answers;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Hit;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.pojos.Ratings;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JSONFullTask {
    private final int id;
    private final String timestamp;
    private final String type;
    private final int payment;
    private final int bonus;
    private final Integer rating;
    private final String platform;

    public JSONFullTask(int id, String timestamp, String type, int payment, int bonus, int rating, String platform) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
        this.rating = rating;
        this.platform = platform;
    }

    public JSONFullTask(Hit hit, Answers answers){
        this.id = answers.getIdanswers();
        this.timestamp = answers.getTimestamp().toString();
        //TODO: after merge with database replace with enum
        this.type = "ANSWERS";
        this.payment = hit.getPayment();
        this.bonus = hit.getBonus();
        this.rating = null;
        this.platform = hit.getCrowdPlatform();
    }

    public JSONFullTask(Hit hit, Ratings ratings) {
        this.id = ratings.getIdratings();
        this.timestamp = ratings.getTimestamp().toString();
        //TODO: after merge with database replace with enum
        this.type = "RATINGS";
        this.payment = hit.getPayment();
        this.bonus = hit.getBonus();
        this.rating = ratings.getRating();
        this.platform = hit.getCrowdPlatform();
    }
}
