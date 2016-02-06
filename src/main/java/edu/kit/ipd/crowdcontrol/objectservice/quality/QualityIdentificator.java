package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.ExperimentController;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.*;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AlgorithmOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityByRatings;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityStrategy;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityByDistribution;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityStrategy;
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
public class QualityIdentificator{


    final static int MAXIMUM_QUALITY = 9;
    final static int MINIMUM_QUALITY = 0;

    private final Logger log = LogManager.getLogger(QualityIdentificator.class);
    private final Observable<Event<Rating>> ratingObservable = EventManager.RATINGS_CREATE.getObservable();
    private final ExperimentController controller;
    private final AnswerRatingOperations answerOperations;
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
    private int ratingQualityThreshold = 5;

    public QualityIdentificator(AlgorithmOperations algorithmOperations, AnswerRatingOperations answerRatingOperations, ExperimentOperations experimentOperations, ExperimentController controller) {

        this.controller = controller;
        this.answerOperations = answerRatingOperations;
        this.experimentOperations = experimentOperations;
        this.algorithmOperations = algorithmOperations;
        this.answerAlgorithms = new HashSet<>();
        this.ratingAlgorithms = new HashSet<>();

        // Reflection isn't used, that's why algorithms need to be added manually
        //------------------------------------------------------
        //
        //           ADD OTHER ALGORITHMS HERE
        //
        //------------------------------------------------------
        answerAlgorithms.add(new AnswerQualityByRatings());
        ratingAlgorithms.add(new RatingQualityByDistribution());



        //Load algorithms in db
        answerAlgorithms.forEach(algorithm -> algorithmOperations.storeAnswerQualityAlgorithm(new AlgorithmAnswerQualityRecord(algorithm.getAlgorithmName(),algorithm.getAlgorithmDescription()),algorithm.getParams()));
        ratingAlgorithms.forEach(algorithm -> algorithmOperations.storeRatingQualityAlgorithm(new AlgorithmRatingQualityRecord(algorithm.getAlgorithmName(),algorithm.getAlgorithmDescription()),algorithm.getParams()));

        ratingObservable.subscribe(rating -> this.onNext(rating.getData()));

    }

    /**
     * This method is performed, if the RATINGS_CREATE-observable emits an event.
     * All ratings and answers of the experiment will be rated. Furthermore the status of the experiment is checked and
     * if the criteria are met, it will be shut-down
     * @param rating which has been created an will be processed
     */
    private void onNext(Rating rating) {

        Optional<ExperimentRecord> exp = experimentOperations.getExperiment(rating.getExperimentId());
        if (!exp.isPresent()) {
            throw new IllegalArgumentException("Error! Can't retrieve the experiment matching to ID:" + rating.getExperimentId());
        }

        try {
            ratingIdentifier = getRatingQualityAlgorithm(exp.get().getAlgorithmQualityRating()).get();
        } catch (NoSuchElementException e1) {
            log.fatal("Error! Could not find %s-algorithm. Replacing with default RatingQualityByDistribution-algorithm.", exp.get().getAlgorithmQualityRating(), e1);
            ratingIdentifier = new RatingQualityByDistribution();
        }
        try {
            answerIdentifier = getAnswerQualityAlgorithm(exp.get().getAlgorithmQualityAnswer()).get();
        } catch (NoSuchElementException e1) {
            log.fatal("Error! Could not find %s-algorithm. Replacing with default AnswerQualityByRatings-algorithm.", exp.get().getAlgorithmQualityAnswer(), e1);
            answerIdentifier = new AnswerQualityByRatings();
        }


        algorithmOperations.getAnswerQualityParams(ratingIdentifier.getAlgorithmName(), exp.get().getIdExperiment()).entrySet().forEach(entry ->{
            if(entry.getKey().field5().toString().equals("RatingQualityThreshold")){    //DOES THIS COMPARISON WORK ?
                ratingQualityThreshold = Integer.valueOf(entry.getValue());
                if(ratingQualityThreshold < 0 || ratingQualityThreshold > 9){
                    log.fatal(String.format("Error! Received illegal argument for %s! Should: %s   Is: %s", entry.getKey().field4(),entry.getKey().field3(),entry.getValue()));
                }
            }
        });


        rateQualityOfAnswers(exp.get());
        rateQualityOfRatings(exp.get());
        checkExpStatus(exp.get());


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
        if (experiment.getNeededAnswers() == answerOperations.getNumberOfFinalGoodAns(experiment.getIdExperiment())) {
            controller.endExperiment(ExperimentTransformer.toProto(experiment, experimentOperations.getExperimentState(experiment.getIdExperiment())));
        }

    }


    /**
     * Rates and sets quality of all ratings of specified experiment.
     * Ratings to the same answer are grouped and rated together.
     *
     * @param experimentRecord whose ratings' qualities are going to be estimated
     */
    private void rateQualityOfRatings(ExperimentRecord experimentRecord) {

        List<AnswerRecord> answers = answerOperations.getAnswersOfExperiment(experimentRecord.getIdExperiment());

        for (AnswerRecord answer : answers) {
            List<RatingRecord> records = answerOperations.getRatingsOfAnswer(answer);
            Map<RatingRecord, Integer> map = ratingIdentifier.identifyRatingQuality(records, MAXIMUM_QUALITY, MINIMUM_QUALITY);
            answerOperations.setQualityToRatings(map);
        }

    }


    /**
     * Rates and sets quality of all answers of specified experiment.
     * Only uses ratings of a specified quality (@ratingQualityThreshold)
     * Furthermore checks if a specified amount of ratings is present for that answer
     * and it thus the answer's quality is unlikely to change. In that case the corresponding
     * quality-assured-bit is set in the database.
     *
     * @param experimentRecord the experiment whose answers are going to be rated
     */
    private void rateQualityOfAnswers(ExperimentRecord experimentRecord) {

        List<AnswerRecord> answers = answerOperations.getAnswersOfExperiment(experimentRecord.getIdExperiment());

        for (AnswerRecord answer : answers) {
            List<RatingRecord> records = answerOperations.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);
            if (records.size() > 0) {
                answerOperations.setQualityToAnswer(answer, answerIdentifier.identifyAnswerQuality(answer, records, MAXIMUM_QUALITY, MINIMUM_QUALITY));

                // Checks if quality_assured bit can be set.
                if ((((double) records.size() / (double) experimentRecord.getRatingsPerAnswer()) >= 0.8)) {
                    answerOperations.setAnswerQualityAssured(answer);
                }
            }
        }
    }

}
