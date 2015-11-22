package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * @author LeanderK
 * @version 1.0
 */
public enum HitType {
    ANSWER, RATING;

    //TODO: UNIT TEST!
    public int getAmount(int total, int ratingToAnswer) {
        if (this == ANSWER) {
            return (int) (total * (1/(ratingToAnswer + 1D)));
        } else {
            return (int) (total * (1/(ratingToAnswer*(ratingToAnswer + 1D))));
        }
    }

    public String getPlatform(String answerPlatform, String ratingPlatform) {
        if (this == ANSWER) {
            return answerPlatform;
        } else {
            return ratingPlatform;
        }
    }
}
