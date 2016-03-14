package edu.kit.ipd.crowdcontrol.objectservice.config;

import edu.kit.ipd.crowdcontrol.objectservice.mail.MailSend;

/**
 * Mail variables in the config.
 *
 * @author LeanderK
 */
public class Mail {
    /**
     * If true the mail will just print to the command line, useful for a development environment.
     */
    public boolean disabled;

    public MailSender notifications, moneytransfer;

    public MailReceiver moneyReceiver;
}
