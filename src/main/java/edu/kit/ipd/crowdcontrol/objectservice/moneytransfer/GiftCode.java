package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

/**
 * Describes an Amazon giftcode
 * @author Felix Rittler
 */
public class GiftCode {

    private String code;
    private int amount; //in ct

    /**
     * Creates a new GiftCode object.
     * @param code the giftcode
     * @param amount the amount of money in ct of the giftcode
     */
    public GiftCode(String code, int amount) {
        this.code = code;
        this.amount = amount;
    }

    /**
     * Returns the giftcode.
     * @return the giftcode
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the amount of money of the giftcode.
     * @return the amount of money of the giftcode in ct
     */
    public int getAmount() {
        return amount;
    }
}
