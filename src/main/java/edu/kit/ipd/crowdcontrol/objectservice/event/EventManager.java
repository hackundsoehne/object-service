package edu.kit.ipd.crowdcontrol.objectservice.event;

import edu.kit.ipd.crowdcontrol.objectservice.proto.*;

/**
 * Carries all predefined events.
 *
 * @author Marcel Hollerbach
 * @author Niklas Keller
 */
public class EventManager {
    public static final EventObservable<Experiment> EXPERIMENT_CREATE = new EventObservable<>();
    public static final EventObservable<ChangeEvent<Experiment>> EXPERIMENT_CHANGE = new EventObservable<>();
    public static final EventObservable<Experiment> EXPERIMENT_DELETE = new EventObservable<>();
    public static final EventObservable<Answer> ANSWER_CREATE = new EventObservable<>();
    public static final EventObservable<Rating> RATINGS_CREATE = new EventObservable<>();
    public static final EventObservable<Worker> WORKER_CREATE = new EventObservable<>();
    public static final EventObservable<Worker> WORKER_CHANGE = new EventObservable<>();
    public static final EventObservable<Worker> WORKER_DELETE = new EventObservable<>();
    public static final EventObservable<CalibrationAnswer> WORKER_CALIBRATION_CREATE = new EventObservable<>();
    public static final EventObservable<Template> TEMPLATE_CREATE = new EventObservable<>();
    public static final EventObservable<ChangeEvent<Template>> TEMPLATE_UPDATE = new EventObservable<>();
    public static final EventObservable<Template> TEMPLATE_DELETE = new EventObservable<>();
    public static final EventObservable<Calibration> CALIBRATION_CREATE = new EventObservable<>();
    public static final EventObservable<Calibration> CALIBRATION_DELETE = new EventObservable<>();
    public static final EventObservable<Notification> NOTIFICATION_CREATE = new EventObservable<>();
    public static final EventObservable<ChangeEvent<Notification>> NOTIFICATION_UPDATE = new EventObservable<>();
    public static final EventObservable<Notification> NOTIFICATION_DELETE = new EventObservable<>();
}
