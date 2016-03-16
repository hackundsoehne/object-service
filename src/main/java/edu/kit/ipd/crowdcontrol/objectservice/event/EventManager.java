package edu.kit.ipd.crowdcontrol.objectservice.event;

import edu.kit.ipd.crowdcontrol.objectservice.proto.*;

/**
 * Carries all predefined events.
 *
 * @author Marcel Hollerbach
 * @author Niklas Keller
 */
public class EventManager {
    public final EventObservable<Experiment> EXPERIMENT_CREATE;
    public final EventObservable<ChangeEvent<Experiment>> EXPERIMENT_CHANGE;
    public final EventObservable<Experiment> EXPERIMENT_DELETE;
    public final EventObservable<Answer> ANSWER_CREATE;
    public final EventObservable<Rating> RATINGS_CREATE;
    public final EventObservable<Worker> WORKER_CREATE;
    public final EventObservable<Worker> WORKER_CHANGE;
    public final EventObservable<Worker> WORKER_DELETE;
    public final EventObservable<CalibrationAnswer> WORKER_CALIBRATION_CREATE;
    public final EventObservable<Template> TEMPLATE_CREATE;
    public final EventObservable<ChangeEvent<Template>> TEMPLATE_UPDATE;
    public final EventObservable<Template> TEMPLATE_DELETE;
    public final EventObservable<Calibration> CALIBRATION_CREATE;
    public final EventObservable<Calibration> CALIBRATION_DELETE;
    public final EventObservable<Notification> NOTIFICATION_CREATE;
    public final EventObservable<ChangeEvent<Notification>> NOTIFICATION_UPDATE;
    public final EventObservable<Notification> NOTIFICATION_DELETE;

    public EventManager() {
        EXPERIMENT_CREATE = new EventObservable<>();
        EXPERIMENT_CHANGE = new EventObservable<>();
        EXPERIMENT_DELETE = new EventObservable<>();
        ANSWER_CREATE = new EventObservable<>();
        RATINGS_CREATE = new EventObservable<>();
        WORKER_CREATE = new EventObservable<>();
        WORKER_CHANGE = new EventObservable<>();
        WORKER_DELETE = new EventObservable<>();
        WORKER_CALIBRATION_CREATE = new EventObservable<>();
        TEMPLATE_CREATE = new EventObservable<>();
        TEMPLATE_UPDATE = new EventObservable<>();
        TEMPLATE_DELETE = new EventObservable<>();
        CALIBRATION_CREATE = new EventObservable<>();
        CALIBRATION_DELETE = new EventObservable<>();
        NOTIFICATION_CREATE = new EventObservable<>();
        NOTIFICATION_UPDATE = new EventObservable<>();
        NOTIFICATION_DELETE = new EventObservable<>();
    }
}
