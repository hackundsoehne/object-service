package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by lucaskrauss at 02.02.2016
 *
 * @author lucaskrauss
 *         <p>
 *         This class provides the functionallity to find duplicates within all answers of an experiment
 */
public class DuplicateChecker {

    private final AnswerRatingOperations answerRatingOperations;

    public DuplicateChecker(AnswerRatingOperations answerRatingOperations) {
        this.answerRatingOperations = answerRatingOperations;
    }


    /**
     * Checks all answers of an experiment for duplicates.
     * If duplicates are found their quality is set to 0 and their quality_assured bit is set.
     * The original answer keeps its quality.
     *
     * @param experiment the experiment
     */
    //Only work for string-answers for now
    public void checkExperimentForDuplicates(ExperimentRecord experiment) {


            //get duplicates
            Set<AnswerRecord> duplicates = new HashSet<>();

            if (experiment.getAnswerType() == null) {
                Map<AnswerRecord,Long>mapOfHashes =  new HashMap<>(); //TODO replace with db op
                if(mapOfHashes.size() > 1) {
                    duplicates.addAll(getStringDuplicates(mapOfHashes));
                }
            }else{
                Map<AnswerRecord,Color[][]> mapOfSignatures = new HashMap<>(); //TODO replace with db-op
                        if(mapOfSignatures.size() > 1){
                duplicates.addAll(getImageDuplicates(mapOfSignatures));
            }
            }

            duplicates.forEach(answerRecord -> {
                answerRatingOperations.setQualityToAnswer(answerRecord, 0);
                answerRatingOperations.setAnswerQualityAssured(answerRecord);
            });
        }




    private Set<AnswerRecord> getStringDuplicates(Map<AnswerRecord,Long> mapOfHashes) {
        Set<AnswerRecord> duplicates = new HashSet<>();

        //Caparison for small number of answers
        if (mapOfHashes.size() < 100) {

            mapOfHashes.forEach((answerRecordA, hashA) -> {
                mapOfHashes.forEach((answerRecordB, hashB) -> {
                    if (StringSimilarity.getSimilarityFromHash(hashA, hashB) > 0.85 && !answerRecordA.equals(answerRecordB)) {
                        duplicates.add(answerRecordA.getTimestamp().compareTo(answerRecordB.getTimestamp()) < 0 ? answerRecordB : answerRecordA);
                    }
                });
            });


        //Comparison for larger numbers of answers
        } else {


            List<Map.Entry<AnswerRecord, Long>> sortedEntries = new ArrayList<>(mapOfHashes.entrySet());

            //rotate, sort and compare |hash|-times
            for (int i = 0; i < 64; i++) {
                Collections.sort(sortedEntries, (a, b) -> Long.compareUnsigned(a.getValue(), b.getValue()));

                for (int j = 0; j < sortedEntries.size() - 1; j++) {
                    if (StringSimilarity.getHammingDistanceOfHashes(sortedEntries.get(j).getValue(), sortedEntries.get(j + 1).getValue()) > 0.85) {
                        duplicates.add(
                                sortedEntries.get(j).getKey().getTimestamp().compareTo(sortedEntries.get(j + 1).getKey().getTimestamp()) < 0 ? sortedEntries.get(j + 1).getKey() : sortedEntries.get(j).getKey());
                    }
                }
                if(i < 63){
                sortedEntries.forEach(answerRecordLongEntry -> answerRecordLongEntry.setValue(Long.rotateLeft(answerRecordLongEntry.getValue(),1)));
            }
            }


        }
        return duplicates;

    }

    private Set<AnswerRecord> getImageDuplicates(Map<AnswerRecord, Color[][]> mapOfSignatures ) {
        Set<AnswerRecord> duplicates = new HashSet<>();

        mapOfSignatures.forEach((answerRecordA, signatureA) ->{
            mapOfSignatures.forEach((answerRecordB, signatureB) ->{
                if(answerRecordA != answerRecordB){
                    if(ImageSimilarity.identifyImageSignatureSimilarity(signatureA,signatureB) > .92){
                        duplicates.add(answerRecordA.getTimestamp().compareTo(answerRecordB.getTimestamp()) < 0 ? answerRecordB : answerRecordA);
                    }
                }
            });
        });

        return duplicates;
    }


}
