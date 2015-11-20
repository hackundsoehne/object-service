package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * Created by marcel on 20.11.15.
 */
public class Nop implements CrowdPlatform {
    @Override
    public boolean publishTask(String id, String name, String url, int amount) {
        System.out.println("Publish "+id+" name:"+name+" url:"+url+" amount:"+amount);
        return true;
    }

    @Override
    public boolean updateTask(String id, String name, String url, int amount) {
        System.out.println("Update "+id+" name:"+name+" url:"+url+" amount:"+amount);
        return true;
    }

    @Override
    public boolean unpublishTask(String id) {
        System.out.println("Unpublish "+id);
        return true;
    }

    @Override
    public boolean payTask(String answer_id, int payment) {
        System.out.println("Pay "+answer_id+" with "+payment);
        return true;
    }

    /**
     * the name of the CrowdPlatform
     *
     * @return true if successful, false if not
     */
    @Override
    public String getName() {
        return "NOP";
    }
}
