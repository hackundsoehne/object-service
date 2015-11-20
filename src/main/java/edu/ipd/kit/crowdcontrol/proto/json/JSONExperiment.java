package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.RatingoptionsRecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JSONExperiment {
    private final int budget;
    private final String question;
    private final String ratingTaskQuestion;
    private final String task_picture_url;
    private final String task_picture_license_url;
    private final String task_description;
    private final String hit_title;
    private final String hit_description;
    //Integer on purpose, because it may not be present in JSON
    private final Integer basicPaymentHIT;
    private final Integer basicPaymentAnswer;
    private final Integer bonusPayment;
    private final Integer basicPaymentRating;
    private final Integer maxAnswersPerAssignment;
    private final Integer maxRatingsPerAssignment;
    private final Map<String, Double> ratingOptions;

    public JSONExperiment(ExperimentRecord experimentRecord, Map<String, Double> ratingOptions) {
        budget = experimentRecord.getBudget();
        question = experimentRecord.getQuestion();
        ratingTaskQuestion = experimentRecord.getTaskquestion();
        task_picture_url = experimentRecord.getTaskPictureUrl();
        task_picture_license_url = experimentRecord.getTaskPictureLicenseUrl();
        task_description = experimentRecord.getTaskDescription();
        hit_title = experimentRecord.getHitTitle();
        hit_description = experimentRecord.getHitDescription();
        basicPaymentHIT = experimentRecord.getBasicpaymenthit();
        basicPaymentAnswer = experimentRecord.getBasicpaymentanswer();
        bonusPayment = experimentRecord.getBonuspayment();
        basicPaymentRating = experimentRecord.getBasicpaymentrating();
        maxAnswersPerAssignment = experimentRecord.getMaxanswersperassignment();
        maxRatingsPerAssignment = experimentRecord.getMaxratingsperassignment();
        this.ratingOptions = ratingOptions;
    }

    public ExperimentRecord createRecord() {
        ExperimentRecord experimentRecord = new ExperimentRecord();
        experimentRecord.setBudget(budget);
        experimentRecord.setQuestion(question);
        experimentRecord.setTaskquestion(ratingTaskQuestion);
        experimentRecord.setTaskPictureUrl(task_picture_url);
        experimentRecord.setTaskPictureLicenseUrl(task_picture_license_url);
        experimentRecord.setTaskDescription(task_description);
        experimentRecord.setHitTitle(hit_title);
        experimentRecord.setHitDescription(hit_description);
        experimentRecord.setBasicpaymenthit(basicPaymentHIT);
        experimentRecord.setBasicpaymentanswer(basicPaymentAnswer);
        experimentRecord.setBonuspayment(bonusPayment);
        experimentRecord.setBasicpaymentrating(basicPaymentRating);
        experimentRecord.setMaxanswersperassignment(maxAnswersPerAssignment);
        experimentRecord.setMaxratingsperassignment(maxRatingsPerAssignment);
        return experimentRecord;
    }

    public List<RatingoptionsRecord> createRatingOptionsRecord() {
        return ratingOptions.entrySet().stream()
                .map(entry -> {
                    RatingoptionsRecord record = new RatingoptionsRecord();
                    record.setKey(entry.getKey());
                    record.setValue(entry.getValue());
                    return record;
                })
                .collect(Collectors.toList());
    }
}
