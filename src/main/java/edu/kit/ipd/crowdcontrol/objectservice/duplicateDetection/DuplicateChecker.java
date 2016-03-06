package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AnswerRatingTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.HashSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.ImageSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;

/**
 * Created by lucaskrauss at 02.02.2016
 *
 * @author lucaskrauss
 *         <p>
 *         This class provides the functionallity to find duplicates within all answers of an experiment
 */
public class DuplicateChecker implements Runnable{

    private final AnswerRatingOperations answerRatingOperations;
    private final BlockingQueue<AnswerRecord> queue = new LinkedBlockingQueue<>();

    public DuplicateChecker(AnswerRatingOperations answerRatingOperations) {
        this.answerRatingOperations = answerRatingOperations;
        EventManager.ANSWER_CREATE.getObservable().subscribe(answerEvent -> {
            try {
                queue.put(AnswerRatingTransformer.toAnswerRecord(answerEvent.getData(), answerEvent.getData().getExperimentId()));
            } catch (InterruptedException e) {
                //TODO react
                e.printStackTrace();
            }
            this.run();
        });
    }

    public void run(){


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


        Map<AnswerRecord, Long> mapOfHashes = new HashMap<>(); //TODO replace with db op
        if (mapOfHashes.size() > 1) {
            duplicates.addAll(getDuplicatesFromHash(mapOfHashes));
        }

        duplicates.forEach(answerRecord -> {
            answerRatingOperations.setQualityToAnswer(answerRecord, 0);
            answerRatingOperations.setAnswerQualityAssured(answerRecord);
        });
    }





    private Set<AnswerRecord> getDuplicatesFromHash(Map<AnswerRecord, Long> mapOfHashes) {
        Set<AnswerRecord> duplicates = new HashSet<>();

        //Caparison for small number of answers
        if (mapOfHashes.size() < 100) {

            mapOfHashes.forEach((answerRecordA, hashA) -> {
                mapOfHashes.forEach((answerRecordB, hashB) -> {
                    if (HashSimilarity.getSimilarityFromHash(hashA, hashB) > 0.85 && !answerRecordA.equals(answerRecordB)) {
                        duplicates.add(answerRecordA.getTimestamp().compareTo(answerRecordB.getTimestamp()) < 0 ? answerRecordB : answerRecordA);
                    }
                });
            });

            //Comparison for larger numbers of answers
        } else {

            List<Map.Entry<AnswerRecord, Long>> sortedEntries = new ArrayList<>(mapOfHashes.entrySet());

            //Sorts hashes and checks each hash with the next greater one for duplicates.
            //Rotating and sorting hashes again assures that duplicates will be next to each other at some point of time
            for (int i = 0; i < 64; i++) {
                Collections.sort(sortedEntries, (a, b) -> Long.compareUnsigned(a.getValue(), b.getValue()));
                for (int j = 0; j < sortedEntries.size() - 1; j++) {
                    if (HashSimilarity.getHammingDistanceOfHashes(sortedEntries.get(j).getValue(), sortedEntries.get(j + 1).getValue()) > 0.85) {
                        duplicates.add(
                                sortedEntries.get(j).getKey().getTimestamp().compareTo(sortedEntries.get(j + 1).getKey().getTimestamp()) < 0 ? sortedEntries.get(j + 1).getKey() : sortedEntries.get(j).getKey());
                    }
                }
                if (i < 63) {
                    sortedEntries.forEach(answerRecordLongEntry -> answerRecordLongEntry.setValue(Long.rotateLeft(answerRecordLongEntry.getValue(), 1)));
                }
            }
        }
        return duplicates;

    }


    @Deprecated
    private Set<AnswerRecord> getImageDuplicatesFromSignatures(Map<AnswerRecord, Color[][]> mapOfSignatures) {
        Set<AnswerRecord> duplicates = new HashSet<>();

        mapOfSignatures.forEach((answerRecordA, signatureA) -> {
            mapOfSignatures.forEach((answerRecordB, signatureB) -> {
                if (answerRecordA != answerRecordB) {
                    if (ImageSimilarity.identifyImageSignatureSimilarity(signatureA, signatureB) > .92) {
                        duplicates.add(answerRecordA.getTimestamp().compareTo(answerRecordB.getTimestamp()) < 0 ? answerRecordB : answerRecordA);
                    }
                }
            });
        });

        return duplicates;
    }


}
