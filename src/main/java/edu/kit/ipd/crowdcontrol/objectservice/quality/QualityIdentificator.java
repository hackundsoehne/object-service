package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.ExperimentFetcher;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.ExperimentsPlatformStatusPlatformStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AlgorithmOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentsPlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityByRatings;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityStrategy;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityByDistribution;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityStrategy;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.ExperimentOperator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.*;

/**
 * Created by lucaskrauss
 * <p>
 * The QualityIdentificator class provides the functionality to rate the quality of ratings and answers of
 * an experiment.
 * It is notified via a EXPERIMENT_CHANGE-observable if an experiment has been stopped.
 * In that case the QualityIdentificator will first rate the quality of all ratings of the ended experiment
 * and thus can assure that only "good" ratings will be used on the identification of the experiment's
 * answers.
 */
public class QualityIdentificator {


    final static int MAXIMUM_QUALITY = 9;
    final static int MINIMUM_QUALITY = 0;

    private final Logger log = LogManager.getLogger(QualityIdentificator.class);
    private final Observable<Event<Rating>> ratingObservable;
    private final ExperimentsPlatformOperations experimentsPlatformOperations;
    private final ExperimentOperator experimentOperator;
    private final ExperimentFetcher experimentFetcher;
    private final AnswerRatingOperations answerRatingOperations;
    private final ExperimentOperations experimentOperations;
    private final AlgorithmOperations algorithmOperations;
    private final Set<AnswerQualityStrategy> answerAlgorithms;
    private final Set<RatingQualityStrategy> ratingAlgorithms;

    private AnswerQualityStrategy answerIdentifier;
    private RatingQualityStrategy ratingIdentifier;


    /**
     * Has to be  >= 0 and  <10
     * <p>
     * Might be set to allow more flexibility and more good answers
     */

    public QualityIdentificator(AlgorithmOperations algorithmOperations, AnswerRatingOperations answerRatingOperations, ExperimentOperations experimentOperations, ExperimentOperator experimentOperator, ExperimentsPlatformOperations experimentsPlatformOperations, EventManager eventManager, ExperimentFetcher experimentFetcher) {
        this.experimentsPlatformOperations = experimentsPlatformOperations;
        this.experimentOperator = experimentOperator;
        this.answerRatingOperations = answerRatingOperations;
        this.experimentOperations = experimentOperations;
        this.algorithmOperations = algorithmOperations;
        this.experimentFetcher = experimentFetcher;
        this.answerAlgorithms = new HashSet<>();
        this.ratingAlgorithms = new HashSet<>();
        this.ratingObservable = eventManager.RATINGS_CREATE.getObservable();
        // Reflection isn't used, that's why algorithms need to be added manually
        //------------------------------------------------------
        //
        //           ADD OTHER ALGORITHMS HERE
        //
        //------------------------------------------------------
        answerAlgorithms.add(new AnswerQualityByRatings());
        ratingAlgorithms.add(new RatingQualityByDistribution());


        //Load algorithms in db
        answerAlgorithms.forEach(algorithm -> algorithmOperations.storeAnswerQualityAlgorithm(new AlgorithmAnswerQualityRecord(algorithm.getAlgorithmName(), algorithm.getAlgorithmDescription()), algorithm.getParams()));
        ratingAlgorithms.forEach(algorithm -> algorithmOperations.storeRatingQualityAlgorithm(new AlgorithmRatingQualityRecord(algorithm.getAlgorithmName(), algorithm.getAlgorithmDescription()), algorithm.getParams()));

        ratingObservable.subscribe(rating -> this.onNext(rating.getData()));

    }

    /**
     * This method is performed, if the RATINGS_CREATE-observable emits an event.
     * All ratings and answers of the experiment will be rated. Furthermore the status of the experiment is checked and
     * if the criteria are met, it will be shut-down{
     *
     * @param rating which has been created an will be processed
     */
    private void onNext(Rating rating) {

        ExperimentRecord exp = experimentOperations.getExperiment(rating.getExperimentId()).orElseThrow(() -> new IllegalArgumentException("Error! Can't retrieve the experiment matching to ID:" + rating.getExperimentId()));


        ratingIdentifier = getRatingQualityAlgorithm(exp.getAlgorithmQualityRating()).orElseGet(() -> {
                    log.fatal("Error! Could not find " + exp.getAlgorithmQualityRating() + "-algorithm. Replacing with default RatingQualityByDistribution-algorithm.");
                    return new RatingQualityByDistribution();
                }
        );


        answerIdentifier = getAnswerQualityAlgorithm(exp.getAlgorithmQualityAnswer()).orElseGet(() -> {
            log.fatal("Error! Could not find " + exp.getAlgorithmQualityAnswer() + "-algorithm. Replacing with default AnswerQualityByRatings-algorithm.");
            return new AnswerQualityByRatings();
        });

        AnswerRecord answerRecord = answerRatingOperations.getAnswerFromRating(rating).orElseThrow(IllegalArgumentException::new);
        rateQualityOfRatings(answerRecord, algorithmOperations.getRatingQualityParams(ratingIdentifier.getAlgorithmName(), exp.getIdExperiment()));
        rateQualityOfAnswers(exp,answerRecord, algorithmOperations.getAnswerQualityParams(answerIdentifier.getAlgorithmName(), exp.getIdExperiment()));

        checkExpStatus(exp);


    }


    /**
     * Performs a look-up on all provided answerQualityAlgorithms
     *
     * @param name of the wanted algorithm
     * @return An Optional-object containing the algorithm or null if the algorithm isn't present
     */
    private Optional<AnswerQualityStrategy> getAnswerQualityAlgorithm(String name) {

        for (AnswerQualityStrategy algo : answerAlgorithms) {
            if (algo.getAlgorithmName().equals(name)) {
                return Optional.of(algo);
            }
        }
        return Optional.empty();
    }


    /**
     * Performs a look-up on all provided ratingQualityAlgorithms
     *
     * @param name of the wanted algorithm
     * @return An Optional-object containing the algorithm or null if the algorithm isn't present
     */
    private Optional<RatingQualityStrategy> getRatingQualityAlgorithm(String name) {
        for (RatingQualityStrategy algo : ratingAlgorithms) {
            if (algo.getAlgorithmName().equals(name)) {
                return Optional.of(algo);
            }
        }
        return Optional.empty();
    }


    /**
     * Checks if the criteria for ending the experiment are met. In that case the experiment will be shut down via
     * the experiment controller
     *
     * @param experiment to be checked
     */
    private void checkExpStatus(ExperimentRecord experiment) {
        Collection<ExperimentsPlatformStatusPlatformStatus> statuses = experimentsPlatformOperations.getExperimentsPlatformStatusPlatformStatuses(experiment.getIdExperiment())
                .values();
        boolean doShutdown = false;
        if (statuses.contains(ExperimentsPlatformStatusPlatformStatus.creative_stopping)) {
            if (answerRatingOperations.allAnswersHaveMaxRatings(experiment.getIdExperiment())) {
                doShutdown = true;
            }
        } else if (experiment.getNeededAnswers() <= answerRatingOperations.getNumberOfFinalGoodAnswers(experiment.getIdExperiment())) {
            doShutdown = true;
        }
        if (doShutdown){
            log.debug("there are enough answers to end the experiment");
            if(!statuses.contains(ExperimentsPlatformStatusPlatformStatus.shutdown)){ //Only shut down if running and not already shutting down
                log.debug("ending experiment");
                Experiment experimentProto = experimentFetcher.fetchExperiment(experiment.getIdExperiment());
                experimentOperator.endExperiment(experimentProto);
            }
        }

    }


    /**
     * Rates and sets quality of all ratings of specified experiment.
     * Ratings to the same answer are grouped and rated together.
     *
     * @param answerRecord whose ratings' qualities are going to be estimated
     * @param params Mapping of parameter-records to the user specified parameters represented as a string
     */
    private void rateQualityOfRatings(AnswerRecord answerRecord, Map<AlgorithmRatingQualityParamRecord, String> params) {
           List<RatingRecord> records = answerRatingOperations.getRelatedRatings(answerRecord.getIdAnswer());
            Map<RatingRecord, Integer> map = ratingIdentifier.identifyRatingQuality(records, params, MAXIMUM_QUALITY, MINIMUM_QUALITY);
            answerRatingOperations.setQualityToRatings(map);
    }


    /**
     * Rates and sets quality of all answers of specified experiment.
     * Only uses ratings of a specified quality
     * Furthermore che//   assertEquals((int) sortedResult.get(sortedResult.lastKey()), exp.getPaymentBase().getValue() + (exp.getPaymentAnswer().getValue() * workerAnswerMap.get(workerTwo)));cks if a specified amount of ratings is present for that answer
     * and it thus the answer's quality is unlikely to change. In that case the corresponding
     * quality-assured-bit is set in the database.
     *
     * @param answerRecord  which is going to be rated
     * @param params Mapping of parameter-records to the user specified parameters represented as a string
     */
    private void rateQualityOfAnswers(ExperimentRecord experimentRecord,AnswerRecord answerRecord, Map<AlgorithmAnswerQualityParamRecord, String> params) {
        Map<String, Integer> result;
        result = answerIdentifier.identifyAnswerQuality(answerRatingOperations, answerRecord, params, MAXIMUM_QUALITY, MINIMUM_QUALITY);
        answerRatingOperations.setQualityToAnswer(answerRecord, result.get(AnswerQualityStrategy.QUALITY));

        // Checks if quality_assured bit can be set.
        if(hasQualityAssured(experimentRecord,answerRecord,result.get(AnswerQualityStrategy.NUM_OF_RATINGS))){
            answerRatingOperations.setAnswerQualityAssured(answerRecord);
        }

    }


    /**
     * Checks if the quality of the given answer cannot change anymore, even if it receives more ratings
     * @param experimentRecord the answers experiment
     * @param answerRecord the answer
     * @param numberOfRatings number of good ratings for the answer
     * @return true if quality of the answer cannot change anymore
     */
    private boolean hasQualityAssured(ExperimentRecord experimentRecord,AnswerRecord answerRecord, int numberOfRatings) {
        int desiredRatingsPerAnswerOfExperiment = experimentOperations.getExperiment(answerRecord.getExperiment())
                .orElseThrow(() -> new IllegalArgumentException("Error, can't find experiment of given experiment-ID: " + answerRecord.getExperiment())).getRatingsPerAnswer();
    if(numberOfRatings >= desiredRatingsPerAnswerOfExperiment){
        return true;
    }else if((double) numberOfRatings / (double)desiredRatingsPerAnswerOfExperiment >= 0.8){
            int qualityOfAnswer = answerRatingOperations.getAnswer(answerRecord.getIdAnswer()).orElseThrow(() -> new IllegalArgumentException("Error, can't find answer of given answer-ID :"+answerRecord.getIdAnswer())).getQuality();
            int qualityThreshold = experimentRecord.getResultQualityThreshold();
            if(qualityOfAnswer < qualityThreshold){
                if( (Math.round(qualityOfAnswer*numberOfRatings + (desiredRatingsPerAnswerOfExperiment - numberOfRatings)*MAXIMUM_QUALITY )/ (double) desiredRatingsPerAnswerOfExperiment) < qualityThreshold){ //Checks if given answer with below-qualityThreshold-quality
                    return true;                                                                                                                                                                                //stays below the threshold, even if it would receive only
                }                                                                                                                                                                                               //maximum ratings from now.
            }else {
                if( (Math.round(qualityOfAnswer*numberOfRatings + (desiredRatingsPerAnswerOfExperiment - numberOfRatings)*MINIMUM_QUALITY )/ (double) desiredRatingsPerAnswerOfExperiment) >= qualityThreshold){//Checks if given answer with over-qualityThreshold-quality
                    return true;                                                                                                                                                                                //stays above the threshold, even if it would receive only
                }                                                                                                                                                                                               //ratings with MINIMUM_QUALITY from now on.
            }
        }
        return false;
    }
}


