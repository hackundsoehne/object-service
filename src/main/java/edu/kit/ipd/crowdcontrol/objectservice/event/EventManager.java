package edu.kit.ipd.crowdcontrol.objectservice.event;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.Answers;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.Experiment;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.Ratings;

/**
 * Created by marcel on 02.01.16.
 */
public class EventManager {
    public static final EventObservable<Experiment> NEW_EXPERIMENT = new EventObservable<>();
    public static final EventObservable<ChangeEvent<Experiment>> CHANGE_EXPERIMENT = new EventObservable<>();
    public static final EventObservable<Experiment> DEL_EXPERIMENT = new EventObservable<>();
    public static final EventObservable<Answers> NEW_ANSWER = new EventObservable<>();
    public static final EventObservable<Ratings> NEW_RATINGS = new EventObservable<>();
}
