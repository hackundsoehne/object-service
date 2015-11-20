package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.ExperimentRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.QualificationsRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.TagsRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class does not represent the Experiment stored in the Database. It models the JSON-Representation of an Experiment.
 * @author LeanderK
 * @version 1.0
 */
public class JSONExperiment {
    private final Integer id;
    private final String picture_url;
    private final String picture_license_url;
    private final String question;
    private final String titel;
    private final Integer max_ratings_per_assignment;
    private final Integer max_answers_per_assignment;
    private final List<String> tags;
    private final List<String> qualifications;
    private final String ratingOptions;

    public JSONExperiment(ExperimentRecord experimentRecord, List<String> tags, List<String> qualifications) {
        id = experimentRecord.getIdexperiment();
        picture_url = experimentRecord.getPictureUrl();
        picture_license_url = experimentRecord.getPictureLicenseUrl();
        question = experimentRecord.getQuestion();
        titel = experimentRecord.getTitel();
        max_answers_per_assignment = experimentRecord.getMaxAnswersPerAssignment();
        max_ratings_per_assignment = experimentRecord.getMaxRatingsPerAssignment();
        this.tags = tags;
        this.qualifications = qualifications;
        ratingOptions = experimentRecord.getRatingOptions();
    }

    public ExperimentRecord createRecord() {
        ExperimentRecord experimentRecord = new ExperimentRecord();
        experimentRecord.setPictureUrl(picture_url);
        experimentRecord.setPictureLicenseUrl(picture_license_url);
        experimentRecord.setQuestion(question);
        experimentRecord.setTitel(titel);
        experimentRecord.setMaxAnswersPerAssignment(max_answers_per_assignment);
        experimentRecord.setMaxRatingsPerAssignment(max_ratings_per_assignment);
        experimentRecord.setRatingOptions(ratingOptions);
        return experimentRecord;
    }

    public List<TagsRecord> getTags() {
        return tags.stream()
                .map(tag -> {
                    TagsRecord tagsRecord = new TagsRecord();
                    tagsRecord.setTag(tag);
                    tagsRecord.setExperimentT(id);
                    return tagsRecord;
                })
                .collect(Collectors.toList());
    }

    public List<QualificationsRecord> getQualifications() {
        return qualifications.stream()
                .map(qualification -> {
                    QualificationsRecord qualificationsRecord =  new QualificationsRecord();
                    qualificationsRecord.setExperimentQ(id);
                    qualificationsRecord.setText(qualification);
                    return qualificationsRecord;
                })
                .collect(Collectors.toList());
    }
}
