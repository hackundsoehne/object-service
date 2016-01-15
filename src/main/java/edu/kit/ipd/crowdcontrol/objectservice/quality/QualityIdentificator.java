package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventObservable;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.jooq.DSLContext;
import rx.Observable;
import rx.Observer;

import java.util.Iterator;
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
public class QualityIdentificator implements Observer<ChangeEvent<Experiment>> {



    private Observable observable = EventManager.EXPERIMENT_CHANGE.getObservable();
    private AnswerRatingOperations operations;
    private AnswerQualityIdentification answerIdentifier;
    private RatingQualityIdentification ratingIdentifier;


    /**
     * Has to be  >= 0 and  <10
     *
     * Might be set to allow more flexibility and more good answers
     * //TODO ?
     */
    private int ratingQualityThreshold = 10;


    public QualityIdentificator(AnswerRatingOperations ops){
        observable.subscribe();
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


    /**
     * If an ending experiment is detected (via EXPERIMENT_CHANGE-observable),
     * the quality of its ratings and answers are identified.
     *
     * @param changeEvent
     */
    @Override
    public void onNext(ChangeEvent<Experiment> changeEvent) {

        if( (changeEvent.getOld().getState() != Experiment.State.STOPPED)
                && (changeEvent.getNeww().getState() == Experiment.State.STOPPED) ){

            rateQualityOfRatings(changeEvent.getNeww().getId());
            rateQualityOfAnswers(changeEvent.getNeww().getId());

        }
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

            Map<RatingRecord,Integer> map = ratingIdentifier.rateRatings(records);
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
            List<RatingRecord> records = operations.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);


        }




    }


  /*
    Setter methods might be needed if more strategies are added to RatingQualityIdentification
    and AnswerQualityIdentification

    public void setAnswerIdentifier(AnswerQualityIdentification answerIdentifier) {
        this.answerIdentifier = answerIdentifier;
    }

    public void setRatingIdentifier(RatingQualityIdentification ratingIdentifier) {
        this.ratingIdentifier = ratingIdentifier;
    }

 */





}
