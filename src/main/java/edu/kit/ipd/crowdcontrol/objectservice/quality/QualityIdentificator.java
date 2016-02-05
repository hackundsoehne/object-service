package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.ExperimentController;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ExperimentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.AnswerRatingOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.ExperimentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.transformers.ExperimentTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.event.Event;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventManager;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import edu.kit.ipd.crowdcontrol.objectservice.quality.answerQuality.AnswerQualityStrategy;
import edu.kit.ipd.crowdcontrol.objectservice.quality.ratingQuality.RatingQualityStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final Logger log = LogManager.getLogger(QualityIdentificator.class);
    private final Observable<Event<Rating>> ratingObservable = EventManager.RATINGS_CREATE.getObservable();
    private final ExperimentController controller;
    private final AnswerRatingOperations answerOperations;
    private final ExperimentOperations experimentOperations;
    private AnswerQualityStrategy answerIdentifier;
    private RatingQualityStrategy ratingIdentifier;


    /**
     * Has to be  >= 0 and  <10
     * <p>
     * Might be set to allow more flexibility and more good answers
     */
    private int ratingQualityThreshold = 10;

   public QualityIdentificator(AnswerRatingOperations answerRatingOperations, ExperimentOperations experimentOperations, ExperimentController controller){
       this.controller = controller;
       this.answerOperations = answerRatingOperations;
        this.experimentOperations = experimentOperations;
        ratingObservable.subscribe();

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
       // ratingIdentifier = experimentOperations.get()  TODO get  exp. specific
        //answerIdentifier = experimentOperations.get()
        rateQualityOfRatings(exp.get());
        rateQualityOfAnswers(exp.get());
        checkExpStatus(exp.get());


    }


    private void checkExpStatus(ExperimentRecord experiment){
        if(experiment.getNeededAnswers() == answerOperations.getNumberOfFinalGoodAns(experiment.getIdExperiment())){
            controller.endExperiment(ExperimentTransformer.toProto(experiment,experimentOperations.getExperimentState(experiment.getIdExperiment())));
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
     *
     * @param experimentRecord the experiment whose answers are going to be rated
     */
    private void rateQualityOfAnswers(ExperimentRecord experimentRecord) {

        List<AnswerRecord> answers = answerOperations.getAnswersOfExperiment(experimentRecord.getIdExperiment());

        for (AnswerRecord answer : answers) {
            List<RatingRecord> records = answerOperations.getGoodRatingsOfAnswer(answer, ratingQualityThreshold);
            if(records.size() > 0) {
                answerOperations.setQualityToAnswer(answer, answerIdentifier.identifyAnswerQuality(answer, records, MAXIMUM_QUALITY, MINIMUM_QUALITY));

                // Checks if quality_assured bit can be set.
                if ((((double) records.size() / (double) experimentRecord.getRatingsPerAnswer()) >= 0.8)) {
                    answerOperations.setAnswerQualityAssured(answer);
                }
            }
        }
    }

}
