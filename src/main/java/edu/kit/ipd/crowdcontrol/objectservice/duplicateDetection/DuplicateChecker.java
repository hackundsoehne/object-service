package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.AnswerRatingTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.HashSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.ImageSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.Shingle;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.StringSimilarity;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by lucaskrauss at 02.02.2016
 *
 * @author lucaskrauss
 *         <p>
 *         This class provides the functionallity to find duplicates within all answers of an experiment
 */
public class DuplicateChecker {

    static final String IMAGE_NOT_READABLE_RESPONSE = "Specified image is not readable";
    static final String URL_MALFORMED_RESPONSE = "Specified URL is malformed or not readable";

    private final Logger logger = LogManager.getLogger(DuplicateChecker.class);
    private final AnswerRatingOperations answerRatingOperations;
    private final ExperimentOperations experimentOperations;
    private final BlockingQueue<AnswerRecord> queue = new LinkedBlockingQueue<>();

    private final int numOfThreads = 1;
    private final ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
    private final List<DuplicateWatcherThread> threads = new ArrayList<>(numOfThreads);
    private boolean threadRunning = false;
    /**
     * Constructor
     *
     * @param answerRatingOperations used to set quality and quality-assured bit to duplicates
     * @param experimentOperations db-operations used to determine the answer-type of an experiment
     */
    public DuplicateChecker(AnswerRatingOperations answerRatingOperations, ExperimentOperations experimentOperations,EventManager eventManager) {

        this.answerRatingOperations = answerRatingOperations;
        this.experimentOperations = experimentOperations;
        for (int i = 0; i < numOfThreads; i++) {
            threads.add(new DuplicateWatcherThread());
        }

        eventManager.ANSWER_CREATE.getObservable().subscribe(answerEvent -> {
            try {
                queue.put(AnswerRatingTransformer.toAnswerRecord(answerEvent.getData(), answerEvent.getData().getExperimentId()));
            } catch (InterruptedException e) {
                logger.fatal("Answer "+answerEvent.getData().getId()+" of experiment "+answerEvent.getData().getExperimentId()
                        +" got interrupted during insertion in the duplicate-checker's queue!");
            }
        });

        threads.forEach((runnable)->executorService.execute(runnable));

    }


     /**
     * Terminates the running duplicateDetector
     */
    public boolean terminate(){
        threadRunning = false;
        executorService.shutdownNow();
        return executorService.isShutdown();

    }

    /**
     * Checks all answers of an experiment for duplicates.
     * If duplicates are found their quality is set to 0 and their quality_assured bit is set.
     * In Addition to that, the duplicates system-response field
     * The original answer keeps its quality.
     *
     * @param submittedAnswer the be checked for duplicates
     * @param answerHash hashing of the answerRecord
     */
    public void processDuplicatesOfExperiment(String answerType,AnswerRecord submittedAnswer, long answerHash) {
        double threshold = (answerType == null) ? .80 : .75;
        List<AnswerRecord> listOfDuplicates = answerRatingOperations.getDuplicates(answerHash,submittedAnswer.getExperiment(),threshold);
        AnswerRecord originalAnswer = submittedAnswer;
        for (AnswerRecord duplicate : listOfDuplicates ) {
            if(duplicate.getTimestamp().compareTo(originalAnswer.getTimestamp()) < 0)
                originalAnswer = duplicate;
        }
        final AnswerRecord finalOriginalAnswer = originalAnswer;
        listOfDuplicates.removeIf(record -> record.getIdAnswer().equals(finalOriginalAnswer.getIdAnswer()));
        listOfDuplicates.forEach((duplicate)-> {
            answerRatingOperations.setSystemResponseField(duplicate,"This answer is considered a duplicate with: \""+ finalOriginalAnswer.getAnswer()+"\"");
            answerRatingOperations.setQualityToAnswer(duplicate,0);
            answerRatingOperations.setAnswerQualityAssured(duplicate);
        });
    }



    /**
     * Retrieves a hash of the given answer based on its type.
     * If an answer is a string answer, its hash is calculated via the corresponding StringSimilarity method.
     * Else if an answer is considered a URL this method tries to acquire the corresponding picture from its external source
     * In the case of failure (e.g. because of a malformed URL or connection issues) the method will return an empty-optional.
     *
     * @param answerRecord the answer which should be hashed
     * @param answerType the type of the answer
     * @return Optional<Long> if the answer could be hashed or an empty-optional otherwise
     */
    private Optional<Long> getHashFromAnswer(AnswerRecord answerRecord, String answerType) {
        if (answerType == null) { //String answer
            return Optional.of(StringSimilarity.computeSimhashFromShingles(Shingle.getShingle(answerRecord.getAnswer(), 3)));
        } else {
            //Picture ulr
            //PictureFetch
            BufferedImage image;
            URL url;
            try {
                url = new URL(answerRecord.getAnswer());
            } catch (MalformedURLException e) {
                answerRatingOperations.setSystemResponseField(answerRecord,URL_MALFORMED_RESPONSE);
                return Optional.empty();
            }
            try {
                image = ImageIO.read(url);
            } catch (IOException e) {
                answerRatingOperations.setSystemResponseField(answerRecord,IMAGE_NOT_READABLE_RESPONSE);
                return Optional.empty();
            }
            if (image == null) {
                answerRatingOperations.setSystemResponseField(answerRecord,IMAGE_NOT_READABLE_RESPONSE);
                return Optional.empty();
            }
            return Optional.of(ImageSimilarity.getImageHashFromSignature(image));
        }
    }


    /**
     * Reinserts answers, which were not checked/rated by the duplicate-checker back in the
     * queue
     */
    public void rescheduleAnswersForDuplicateDetection(int expID){ //TODO add to expOperator
        List<AnswerRecord> unratedAnswers = answerRatingOperations.getAnswersWithoutHash(expID);
        unratedAnswers.forEach((answerRecord -> {
            try {
                queue.put(answerRecord);
            } catch (InterruptedException e) {
                logger.fatal("Answer "+answerRecord.getIdAnswer()+" of experiment "+answerRecord.getExperiment()
                        +" got interrupted during insertion in the duplicate-checker's queue!");
            }
        }));
    }


    private class DuplicateWatcherThread implements Runnable
    {
        /**
         * While running, the DuplicateChecker hashes the answers in the queue and checks them for duplicates.
         */
        public void run() {
            threadRunning = true;
            while (threadRunning) {
                AnswerRecord answerRecord;
                try {
                    answerRecord = queue.take();
                } catch (InterruptedException e) {
                    logger.info("DuplicateChecker terminated!");
                    return;
                }
                if (answerRecord != null) {
                    rescheduleAnswersForDuplicateDetection(answerRecord.getExperiment());
                    String answerType = experimentOperations.getExperiment(answerRecord.getExperiment())
                            .orElseThrow(() -> new IllegalArgumentException("Error! Can't retrieve the experiment matching to ID!")).getAnswerType();
                    //trying to acquire answer-hash
                    Optional<Long> answerHash = getHashFromAnswer(answerRecord,answerType);
                    if (answerHash.isPresent()) {
                        answerRecord.setHash(answerHash.get());
                        answerRatingOperations.updateAnswer(answerRecord);
                        processDuplicatesOfExperiment(answerType,answerRecord, answerHash.get());
                    } else {
                        // If optional is empty and thus the hashing failed, the answers quality will be set to zero.
                        // The reason for the failure is described in the response-field and set in the getHashFromAnswer-method
                        answerRatingOperations.setQualityToAnswer(answerRecord,0);
                        answerRatingOperations.setAnswerQualityAssured(answerRecord);
                    }
                }
            }
            logger.info("DuplicateChecker terminated!");

        }
    }














    @Deprecated //because duplicate-detection is done in the db now
    /**
     * Identifies duplicates of the given answer-hash-mapping.
     * Different algorithms are used based on the number of answers
     * @param mapOfHashes mapping of answers to their hashes
     * @return Set of answers which are considered a duplicate
     */
    private Set<AnswerRecord> getDuplicatesFromHash(Map<AnswerRecord, Long> mapOfHashes) {
        Set<AnswerRecord> duplicates = new HashSet<>();
        //Caparison for small number of answers
        if (mapOfHashes.size() < 100) {
            mapOfHashes.forEach((answerRecordA, hashA) -> {
                mapOfHashes.forEach((answerRecordB, hashB) -> {
                    if (!answerRecordA.equals(answerRecordB) && HashSimilarity.getSimilarityFromHash(hashA, hashB) > 0.85 ) {
                        AnswerRecord duplicateAnswer;
                        AnswerRecord originalAnswer;
                        if(answerRecordA.getTimestamp().compareTo(answerRecordB.getTimestamp()) < 0){
                            duplicateAnswer = answerRecordB;
                            originalAnswer = answerRecordA;
                        }else {
                            originalAnswer = answerRecordB;
                            duplicateAnswer = answerRecordA;
                        }
                        answerRatingOperations.setSystemResponseField(duplicateAnswer,"This answer is considered a duplicate with: \""+originalAnswer.getAnswer()+"\"");
                        duplicates.add(duplicateAnswer);
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
                    if (HashSimilarity.getSimilarityFromHash(sortedEntries.get(j).getValue(), sortedEntries.get(j + 1).getValue()) > 0.85) {
                        AnswerRecord duplicateAnswer;
                        AnswerRecord originalAnswer;
                        if(sortedEntries.get(j).getKey().getTimestamp().compareTo(sortedEntries.get(j + 1).getKey().getTimestamp()) < 0){
                            duplicateAnswer = sortedEntries.get(j + 1).getKey();
                            originalAnswer = sortedEntries.get(j).getKey();
                        }else {
                            duplicateAnswer = sortedEntries.get(j).getKey();
                            originalAnswer = sortedEntries.get(j + 1).getKey();
                        }
                        answerRatingOperations.setSystemResponseField(duplicateAnswer,"This answer is considered a duplicate with: \""+originalAnswer.getAnswer()+"\"");
                        duplicates.add(duplicateAnswer);
                    }
                }
                if (i < 63) {
                    sortedEntries.forEach(answerRecordLongEntry -> answerRecordLongEntry.setValue(Long.rotateLeft(answerRecordLongEntry.getValue(), 1)));
                }
            }
        }
        return duplicates;

    }


    @Deprecated //Deprecated because image-similarity is identified by the image's hash-values now
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
