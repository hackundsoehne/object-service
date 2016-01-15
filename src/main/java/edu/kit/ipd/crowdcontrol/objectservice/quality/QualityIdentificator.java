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
 * Created by lucas on 15.01.16.
 */
public class QualityIdentificator implements Observer<ChangeEvent<Experiment>> {



    private Observable observable = EventManager.EXPERIMENT_CHANGE.getObservable();
    private AnswerRatingOperations operations;
    private AnswerQualityIdentification answerIdentifier;


    private RatingQualityIdentification ratingIdentifier;

    public QualityIdentificator(AnswerRatingOperations ops){
        observable.subscribe();
        operations = ops;
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
    public void onNext(ChangeEvent<Experiment> changeEvent) {
        if( (changeEvent.getOld().getState() != Experiment.State.STOPPED)
                && (changeEvent.getNeww().getState() == Experiment.State.STOPPED) ){

            rateQualityOfRatings(changeEvent.getNeww().getId());

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
    private void rateQualityOfRatings(int expID){

        List<AnswerRecord> answers = operations.getAnswersOfExperiment(expID);

        for (AnswerRecord answer: answers) {
            List<RatingRecord> record = operations.getRatingsOfAnswer(answer);
            Map<RatingRecord,Integer> map = ratingIdentifier.rateRatings(record);
            operations.setQualityToRatings(map);
        }

    }





}
