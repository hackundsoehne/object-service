package edu.ipd.kit.crowdcontrol.objectservice.crowdplatform;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.Worker;

/**
 * Created by marcel on 02.01.16.
 */
public interface Assertion {
    /**
     * Mark a worker for a assertion
     * @param key for example "language"
     * @param value for example "german"
     * @param worker the worker to mark
     */
    void markAssertion(String key, String value, Worker worker);
    /**
     * delete a workers mark of a assertion
     * @param key for example "language"
     * @param value for example "german"
     * @param worker the worker to mark
     */
    void unmarkAssertion(String key, String value, Worker worker);
    /**
     * Check if the worker has the given assertion
     * @param key for example "language"
     * @param value for example "german"
     * @param worker the worker to mark
     */
    void hasAssertion(String key, String value, Worker worker);
}
