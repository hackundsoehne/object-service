package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;

import java.util.List;
import java.util.Optional;

/**
 * Created by marcel on 20.11.15.
 */
//TODO: Marcel...what about the bonus?
public class Hit {
    private final String id;
    private final String title;
    private final String description;
    private final List<String> tags;
    private final int amount;
    private final double payment;
    private final long assignmentDuration;
    private final long hitDuration;
    private final String url;
    private final HitType hitType;

    /**
     * @param id id of the HIT
     * @param title Title
     * @param description Description which should declare the task before the worker accepts the HIT
     * @param tags The tags where the HIT should be listet at
     * @param amount number how often the hit can be accepted
     * @param payment the payment per hit
     * @param assignmentDuration the duration a worker has to complete a assignment
     * @param hitDuration the duration a hit is running
     * @param url the url to open when the hit is accepted
     */
    public Hit(String id, String title, String description, List<String> tags, int amount, double payment, long assignmentDuration, long hitDuration, String url) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.amount = amount;
        this.payment = payment;
        this.assignmentDuration = assignmentDuration;
        this.hitDuration = hitDuration;
        this.url = url;
        this.hitType = null;
    }

    /**
     * @param record the associated Experiment
     * @param type the Type of the Hit
     * @param tags The tags where the HIT should be listet at
     * @param amount number how often the hit can be accepted
     * @param payment the payment per hit
     * @param assignmentDuration the duration a worker has to complete a assignment
     * @param hitDuration the duration a hit is running
     * @param url the url to open when the hit is accepted
     */
    public Hit(ExperimentRecord record, HitType type, List<String> tags, int amount, double payment, long assignmentDuration, long hitDuration, String url) {
        this.id = String.valueOf(record.getIdexperiment());
        this.title = record.getTitel();
        if (type == HitType.ANSWER) {
            description = "produce creative content";
        } else {
            description = "rate creative content produced by others";
        }
        this.tags = tags;
        this.amount = amount;
        this.payment = payment;
        this.assignmentDuration = assignmentDuration;
        this.hitDuration = hitDuration;
        this.url = url;
        this.hitType = type;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public double getPayment() {
        return payment;
    }

    public int getAmount() {
        return amount;
    }

    public long getAssignmentDuration() {
        return assignmentDuration;
    }

    public long getHitDuration() {
        return hitDuration;
    }

    public String getUrl() {
        return url;
    }

    public Optional<HitType> getHitType() {
        return Optional.ofNullable(hitType);
    }

    @Override
    public String toString() {
        return "Hit{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", amount=" + amount +
                ", payment=" + payment +
                ", assignmentDuration=" + assignmentDuration +
                ", hitDuration=" + hitDuration +
                ", url='" + url + '\'' +
                '}';
    }
}
