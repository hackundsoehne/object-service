package edu.kit.ipd.crowdcontrol.objectservice.config;

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

    /**
     * The mail address, that gets displayed as sender in moneytransfer mails.
     */
    public String sendsMailsFrom;


}
