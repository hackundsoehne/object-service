package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final String platform;

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
     * @param platform
     */
    public Hit(String id, String title, String description, List<String> tags, int amount, double payment, long assignmentDuration, long hitDuration, String url, String platform) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.amount = amount;
        this.payment = payment;
        this.assignmentDuration = assignmentDuration;
        this.hitDuration = hitDuration;
        this.url = url;
        this.platform = platform;

    }

    /**
     * returns a new Hit-Object where the ID is changed
     * @param id the new ID
     * @return a new instance of Hit
     */
    public Hit setID(String id) {
        return new Hit(id, title, description, tags, amount, payment, assignmentDuration, hitDuration, url, platform);
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

    public String getPlatform() {
        return platform;
    }

    /**
     * the number of assignment
     * @return -1 or amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * the duration
     * @return -1 or duration
     */
    public long getAssignmentDuration() {
        return assignmentDuration;
    }

    /**
     * the hit-duration
     * @return -1 or hit-duration
     */
    public long getHitDuration() {
        return hitDuration;
    }

    /**
     * the external url
     * @return null or external url
     */
    public String getUrl() {
        return url;
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
