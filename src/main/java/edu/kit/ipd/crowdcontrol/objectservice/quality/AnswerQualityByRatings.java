package edu.kit.ipd.crowdcontrol.objectservice.quality;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;

import java.util.List;

/**
 * Created by lucas on 15.01.16.
 */
public class AnswerQualityByRatings extends AnswerQualityIdentification {



    @Override
    int concreteQuality(AnswerRecord answer, List<RatingRecord> ratings) {
        return 0;
    }
}

