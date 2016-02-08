package edu.kit.ipd.crowdcontrol.objectservice.config;

/**
 * Representation of the moneytransfer section in the config
 */
public class MoneyTransfer {

    /**
     * The minimum amount of money in ct, a worker has to earn, until he gets paid.
     */
    public int payOffThreshold;

    /**
     * The password used, to verify a valid amazon giftcode email (enter password as message at amazon homepage, while buying giftcodes.
     */
    public String parsingPassword;

    /**
     * The mail address to send notification mails to.
     */
    public String notificationMailAddress;

    /**
     * The interval in days, when the workers get paid off.
     */
    public int scheduleInterval;
}
