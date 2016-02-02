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


    private Observable<Event<Rating>> ratingObservable = EventManager.RATINGS_CREATE.getObservable();

    private AnswerRatingOperations operations;
    private ExperimentOperations experimentOperations;
    private AnswerQualityStrategy answerIdentifier;
    private RatingQualityStrategy ratingIdentifier;


    /**
     * Has to be  >= 0 and  <10
     * <p>
     * Might be set to allow more flexibility and more good answers
     */
    private int ratingQualityThreshold = 10;


    public QualityIdentificator(AnswerRatingOperations ops, ExperimentOperations experimentOperations) {

        ratingObservable.subscribe();
        this.operations = ops;
        this.experimentOperations = experimentOperations;
        answerIdentifier = new AnswerQualityByRatings();
        ratingIdentifier = new RatingQualityByDistribution();
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

        int expId = rating.getExperimentId();
        rateQualityOfRatings(expId);
        rateQualityOfAnswers(expId);


    }


    /**
     * Rates and sets quality of all ratings of specified experiment.
     * Ratings to the same answer are grouped and rated together.
     *
     * @param expId of the experiment
     */
    private void rateQualityOfRatings(int expId) {

        List<AnswerRecord> answers = operations.getAnswersOfExperiment(expId);

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
     * @param expId of the experiment
     */
    private void rateQualityOfAnswers(int expId) {

        List<AnswerRecord> answers = operations.getAnswersOfExperiment(expId);
        Optional<ExperimentRecord> experimentRecord =  experimentOperations.getExperiment(expId);

        for (AnswerRecord answer : answers) {
            List<RatingRecord> records = operations.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);
            if(records.size() > 0) {
                operations.setQualityToAnswer(answer, answerIdentifier.identifyAnswerQuality(answer, records, MAXIMUM_QUALITY, MINIMUM_QUALITY));

                // Checks if quality_assured bit can be set.
                if (experimentRecord.isPresent() && (((double) records.size() / (double) experimentRecord.get().getRatingsPerAnswer()) >= 0.8)) {
                    operations.setAnswerQualityAssured(answer);
                }
            }
        }
    }

}
