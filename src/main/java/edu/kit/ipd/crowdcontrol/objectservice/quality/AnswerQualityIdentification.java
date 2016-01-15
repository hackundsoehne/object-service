package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;

import java.util.List;

/**
 * Created by lucaskrauss
 * The AnswerQualityIdentification class identifies the quality of an answer
 * It uses the template-pattern to provide extendability.
 */
public abstract class AnswerQualityIdentification {


    /**
     * Default constuctor
     */
    public AnswerQualityIdentification(){

    }


    public int rateAnswers(AnswerRecord answer, List<RatingRecord> ratings){
        return concreteQuality(answer,ratings);
    }

    abstract int concreteQuality(AnswerRecord answer, List<RatingRecord> ratings);



}
