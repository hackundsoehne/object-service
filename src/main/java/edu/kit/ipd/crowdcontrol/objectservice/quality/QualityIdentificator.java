package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityByRatings;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityStrategy;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityByDistribution;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityStrategy;
import rx.Observable;
import rx.Observer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
public class QualityIdentificator implements Observer<Rating> {


    final static int MAXIMUM_QUALITY = 9;
    final static int MINIMUM_QUALITY = 0;


    private final Observable<Event<Rating>> ratingObservable = EventManager.RATINGS_CREATE.getObservable();
    private final QualityIdentificator qualityIdentificator;
    private final AnswerRatingOperations operations;
    private final ExperimentOperations experimentOperations;
    private AnswerQualityStrategy answerIdentifier;
    private RatingQualityStrategy ratingIdentifier;


    /**
     * Has to be  >= 0 and  <10
     * <p>
     * Might be set to allow more flexibility and more good answers
     */
    private int ratingQualityThreshold = 10;

    private  QualityIdentificator(AnswerRatingOperations answerRatingOperations, ExperimentOperations experimentOperations){
        this.operations = answerRatingOperations;
        this.experimentOperations = experimentOperations;
        ratingObservable.subscribe();
        answerIdentifier = new AnswerQualityByRatings();
        ratingIdentifier = new RatingQualityByDistribution();
        qualityIdentificator = this;
    }
   public static void init(AnswerRatingOperations ops, ExperimentOperations experimentOperations) {
        QualityIdentificator qualityIdentificator = new QualityIdentificator(ops,experimentOperations);
    }


    @Override
    public void onCompleted() {
        //NOP
    }

    @Override
    public void onError(Throwable e) {
        //NOP
    }

    @Override
    public void onNext(Rating rating) {


        Optional<ExperimentRecord> exp = experimentOperations.getExperiment(rating.getExperimentId());
        if(!exp.isPresent()){
            throw new IllegalArgumentException("Error! Can't retrieve the experiment matching to ID:"+rating.getExperimentId());
        }
        //TODO load classes         exp.get().getAlgorithmQualityAnswer()

        qualityIdentificator.rateQualityOfRatings(exp.get());
        qualityIdentificator.rateQualityOfAnswers(exp.get());


    }


    /**
     * Rates and sets quality of all ratings of specified experiment.
     * Ratings to the same answer are grouped and rated together.
     *
     * @param experimentRecord whose ratings' qualities are going to be estimated
     */
    private void rateQualityOfRatings(ExperimentRecord experimentRecord) {

        List<AnswerRecord> answers = operations.getAnswersOfExperiment(experimentRecord.getIdExperiment());

        for (AnswerRecord answer : answers) {
            List<RatingRecord> records = operations.getRatingsOfAnswer(answer);
            Map<RatingRecord, Integer> map = ratingIdentifier.identifyRatingQuality(records, MAXIMUM_QUALITY, MINIMUM_QUALITY);
            operations.setQualityToRatings(map);
        }

    }


    /**
     * Rates and sets quality of all answers of specified experiment.
     * Only uses ratings of a specified quality (@ratingQualityThreshold)
     * Furthermore checks if a specified amount of ratings is present for that answer
     * and it thus the answer's quality is unlikely to change. In that case the corresponding
     * quality-assured-bit is set in the database.
     *
     *
     * @param experimentRecord the experiment whose answers are going to be rated
     */
    private void rateQualityOfAnswers(ExperimentRecord experimentRecord) {



        List<AnswerRecord> answers = operations.getAnswersOfExperiment(experimentRecord.getIdExperiment());

        for (AnswerRecord answer : answers) {
            List<RatingRecord> records = operations.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);
            if(records.size() > 0) {
                operations.setQualityToAnswer(answer, answerIdentifier.identifyAnswerQuality(answer, records, MAXIMUM_QUALITY, MINIMUM_QUALITY));

                // Checks if quality_assured bit can be set.
                if ((((double) records.size() / (double) experimentRecord.getRatingsPerAnswer()) >= 0.8)) {
                    operations.setAnswerQualityAssured(answer);
                }
            }
        }
    }

}
