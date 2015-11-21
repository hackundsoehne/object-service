package edu.ipd.kit.crowdcontrol.proto.json;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JSONRating {
    //TODO: after merge nonull both
    private final Integer answerID;
    private final Integer rating;

    public JSONRating(Integer answerID, Integer rating) {
        this.answerID = answerID;
        this.rating = rating;
    }

    public Integer getAnswerID() {
        return answerID;
    }

    public Integer getRating() {
        return rating;
    }
}
