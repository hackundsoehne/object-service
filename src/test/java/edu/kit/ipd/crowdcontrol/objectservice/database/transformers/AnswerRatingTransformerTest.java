package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingRecord;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Answer;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Rating;
import org.junit.Test;

import static org.junit.Assert.*;

public class AnswerRatingTransformerTest {
    @Test
    public void toAnswerRecord() {
        Answer proto = Answer.newBuilder()
                .setId(3)
                .setExperimentId(6)
                .setContent("foo")
                .setWorker(9)
                .setQuality(13)
                .setTime(5)
                .build();

        AnswerRecord record = AnswerRatingTransformer.toAnswerRecord(proto, 17);

        // ID gets ignored
        assertSame(null, record.getIdAnswer());

        // Explicitly use passed experiment ID
        assertSame(17, record.getExperiment());

        assertEquals("foo", record.getAnswer());
        assertSame(9, record.getWorkerId());

        // Quality gets ignored
        assertSame(null, record.getQuality());

        // Assert that current timestamp is used, not the passed value
        assertTrue(record.getTimestamp().toInstant().getEpochSecond() > 150);
    }

    @Test
    public void toRatingRecord() {
        Rating proto = Rating.newBuilder()
                .setExperimentId(13)
                .setFeedback("foo")
                .setQuality(3)
                .setRating(5)
                .setTime(142)
                .build();

        RatingRecord record = AnswerRatingTransformer.toRatingRecord(proto, 17, 27);

        assertSame(27, record.getExperiment());
        assertSame(5, record.getRating());
        assertSame(17, record.getAnswerR());

        // Quality gets ignored
        assertSame(null, record.getQuality());

        // Assert that current timestamp is used, not the passed value
        assertTrue(record.getTimestamp().toInstant().getEpochSecond() > 150);
    }
}
