package edu.kit.ipd.crowdcontrol.objectservice.rest.resources;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/**
 * @author Niklas Keller
 * @link http://stackoverflow.com/a/10931907/2373138
 */
public class DummyRunner extends Runner {
    @Override
    public Description getDescription() {
        return null;
    }

    public void run(RunNotifier notifier) {
        // Do nothing …
    }
}
