package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.Worker;

/**
 * NOP class should support handling of assertions if a platform does not handle it
 * Created by marcel on 02.01.16.
 */
public class DataBaseAssertion implements Assertion {
    @Override
    public void markAssertion(String key, String value, Worker worker) {

    }

    @Override
    public void unmarkAssertion(String key, String value, Worker worker) {

    }

    @Override
    public void hasAssertion(String key, String value, Worker worker) {

    }
}
