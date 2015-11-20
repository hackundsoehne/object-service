package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;

import java.util.List;

/**
 * Created by marcel on 20.11.15.
 */
//public HIT createHIT(String hitTypeId, String title, String description, String keywords,
// Double reward,
// Long assignmentDurationInSeconds,
// Long autoApprovalDelayInSeconds,
// Long lifetimeInSeconds,
// Integer maxAssignments,
// String requesterAnnotation,
// QualificationRequirement[] qualificationRequirements,
// String[] responseGroup,
// String uniqueRequestToken,
// ReviewPolicy assignmentReviewPolicy,
// ReviewPolicy hitReviewPolicy,
// String hitLayoutId,
// HITLayoutParameter[] hitLayoutParameters)
public class Hit {
    private String id;
    private String title;
    private String description;
    private List<String> tags;
    private int amount;
    private double payment;
    private long assignmentDuration;
    private long hitDuration;
    private String url;

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
}
