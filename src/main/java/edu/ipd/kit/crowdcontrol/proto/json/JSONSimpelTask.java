package edu.ipd.kit.crowdcontrol.proto.json;

import edu.ipd.kit.crowdcontrol.proto.crowdplatform.HitType;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.AnswersRecord;
import edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.RatingsRecord;

/**
 *
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class JSONSimpelTask {
    @JSONRequired
    private final int id;
    private final String timestamp;
    private final String type;

    public JSONSimpelTask(int id, String timestamp, String type) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
    }

    public JSONSimpelTask(AnswersRecord answersRecord) {
        this.id = answersRecord.getIdanswers();
        this.timestamp = answersRecord.getTimestamp().toString();
        this.type = HitType.ANSWER.name();
    }

    public JSONSimpelTask(RatingsRecord ratingsRecord) {
        this.id = ratingsRecord.getIdratings();
        this.timestamp = ratingsRecord.getTimestamp().toString();
        this.type = HitType.RATING.name();
    }
}
