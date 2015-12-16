package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.Tables;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.records.AnswersRecord;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.records.RatingsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Optional;

/**
 * @author LeanderK
 * @version 1.0
 */
public class AnswerRatingOperations extends AbstractOperation {
    protected AnswerRatingOperations(DSLContext create) {
        super(create);
    }

    /**
     * inserts an answer
     * @param answersRecord the answer to insert
     * @return the id of the answer
     */
    public int insertAnswer(AnswersRecord answersRecord) {
        return create.insertInto(Tables.ANSWERS)
                .set(answersRecord)
                .returning()
                .fetchOne()
                .getIdanswers();
    }

    /**
     * inserts the rating
     * @param ratingsRecord the rating to insert
     * @return the id of rating
     */
    public int insertRating(RatingsRecord ratingsRecord) {
        return create.insertInto(Tables.RATINGS)
                .set(ratingsRecord)
                .returning()
                .fetchOne()
                .getIdratings();
    }

    /**
     * reserves a rating for the answer
     * @param answerID the id of the answer
     * @return the resulting id of the empty rating
     */
    public Optional<Integer> reserveRating(int answerID) {
        return create.transactionResult(trans -> DSL.using(trans)
                .selectFrom(Tables.ANSWERS)
                .where(Tables.ANSWERS.IDANSWERS.eq(answerID))
                .fetchOptional()
                .map(answer -> createEmptyRating(answer, trans)));
    }

    private int createEmptyRating (AnswersRecord answer, Configuration trans) {
        return DSL.using(trans)
                .insertInto(Tables.RATINGS)
                .set(new RatingsRecord(null, answer.getHitA(), answer.getIdanswers(), null, null, null))
                .returning()
                .fetchOne()
                .getIdratings();
    }
}
