package playground;

import edu.kit.ipd.crowdcontrol.objectservice.event.ChangeEvent;
import edu.kit.ipd.crowdcontrol.objectservice.event.EventObservable;

/**
 * Created by marcel on 17.12.15.
 */
public class EventSample {
    public static void main(String[] args) {
        EventObservable<ChangeEvent<String>> ob = new EventObservable<>();

        ob.getObservable().subscribe(stringChangeEvent ->
                System.out.println("Change "+stringChangeEvent.getOld()+" to "+stringChangeEvent.getNeww()));


        ob.emit(new ChangeEvent<>("Mensch", "Leander"));
    }
}
