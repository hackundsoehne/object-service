package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
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

/**
 * Created by lucaskrauss
 *
 * The QualityIdentificator class provides the functionality to rate the quality of ratings and answers of
 * an experiment.
 * It is notified via a EXPERIMENT_CHANGE-observable if an experiment has been stopped.
 * In that case the QualityIdentificator will first rate the quality of all ratings of the ended experiment
 * and thus can assure that only "good" ratings will be used on the identification of the experiment's
 * answers.
 *
 *
 *
 */
public class QualityIdentificator implements  Observer<Rating> {



    final static int MAXIMUM_QUALITY = 9;
    final static int MINUMUM_QUALITY = 0;

   // private Observable<Answer> answerObservable = EventManager.ANSWER_CREATE.getObservable();
    private Observable<Rating> ratingObservable = EventManager.RATINGS_CREATE.getObservable();
    private AnswerRatingOperations operations;
    private AnswerQualityStrategy answerIdentifier;
    private RatingQualityStrategy ratingIdentifier;



    /**
     * Has to be  >= 0 and  <10
     *
     * Might be set to allow more flexibility and more good answers
     */
    private int ratingQualityThreshold = 10;


    public QualityIdentificator(AnswerRatingOperations ops){
      //  answerObservable.subscribe();
        //TODO get alg. params
        ratingObservable.subscribe();
        operations = ops;
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


    }





    /**
     * Rates and sets quality of all ratings of specified experiment.
     * Ratings to the same answer are grouped and rated together.
     *
     * @param expID of the experiment.
     */
    private void rateQualityOfRatings(int expID){

        List<AnswerRecord> answers = operations.getAnswersOfExperiment(expID);

        for (AnswerRecord answer: answers) {
            List<RatingRecord> records = operations.getRatingsOfAnswer(answer);

            Map<RatingRecord,Integer> map = ratingIdentifier.identifyRatingQuality(records, MAXIMUM_QUALITY, MINUMUM_QUALITY);
            operations.setQualityToRatings(map);
        }

    }


    /**
     * Rates and sets quality of all answers of specified experiment.
     * Only uses ratings of a specified quality (@ratingQualityThreshold)
     *
     * @param expID of the experiment.
     */
    private void rateQualityOfAnswers(int expID){
        List<AnswerRecord> answers = operations.getAnswersOfExperiment(expID);

        for (AnswerRecord answer: answers) {
            RatingRecord r;
            List<RatingRecord> records = operations.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);


        }




    }

    public static int getMinumumQuality() {
        return MINUMUM_QUALITY;
    }

    public static int getMaximumQuality() {
        return MAXIMUM_QUALITY;
    }


  /*
    Setter methods might be needed if more strategies are added to RatingQualityIdentification
    and AnswerQualityIdentification

    public void setAnswerIdentifier(AnswerQualityStrategy answerIdentifier) {
        this.answerIdentifier = answerIdentifier;
    }

    public void setRatingIdentifier(RatingQualityStrategy ratingIdentifier) {
        this.ratingIdentifier = ratingIdentifier;
    }

 */





}
