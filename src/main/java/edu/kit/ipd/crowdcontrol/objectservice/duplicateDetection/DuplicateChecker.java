package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;

import java.util.*;

/**
 * Created by lucaskrauss at 02.02.2016
 *
 * @author lucaskrauss
 *
 * This class provides the functionallity to find duplicates within all answers of an experiment
 *
 */
public class DuplicateChecker {

    private final AnswerRatingOperations answerRatingOperations;

    public DuplicateChecker(AnswerRatingOperations answerRatingOperations){
        this.answerRatingOperations = answerRatingOperations;
    }


    /**
     * Checks all answers of an experiment for duplicates.
     * If duplicates are found their quality is set to 0 and their quality_assured bit is set.
     * The original answer keeps its quality.
     * @param expID id of the experiment
     */
    //Only work for string-answers for now
    public void checkExperimentForDuplicates(int expID){
        List<AnswerRecord> answerRecords = answerRatingOperations.getAnswersOfExperiment(expID);
        if(answerRecords.size() > 1){
            //get duplicates
            Set<AnswerRecord> duplicates = getStringDuplicates(answerRecords);
            //set quality
            duplicates.forEach(answerRecord -> {
                answerRatingOperations.setQualityToAnswer(answerRecord,0);
                answerRatingOperations.setAnswerQualityAssured(answerRecord);
            });
        }


    }

    private Set<AnswerRecord> getStringDuplicates(List<AnswerRecord> answerRecords){
        Set<AnswerRecord> duplicates = new HashSet<>();
        //Simhashing
        //Retrieve hashes for all answers
        Map<AnswerRecord,Long> mapOfHashes = new HashMap<>();
        answerRecords.forEach(answerRecord ->
                mapOfHashes.put(answerRecord,StringSimilarity.computeSimhashFromShingles(Shingle.getShingle(answerRecord.getAnswer()))));

        mapOfHashes.forEach((answerRecordA,hashA) ->{
            mapOfHashes.forEach((answerRecordB, hashB) ->{
                if (StringSimilarity.getSimilarityFromHash(hashA,hashB) > 0.8 && !answerRecordA.equals(answerRecordB)){
                    duplicates.add( answerRecordA.getTimestamp().compareTo(answerRecordB.getTimestamp()) < 0 ? answerRecordB : answerRecordA  );
                }
            });
        });

        return duplicates;

    }
}
