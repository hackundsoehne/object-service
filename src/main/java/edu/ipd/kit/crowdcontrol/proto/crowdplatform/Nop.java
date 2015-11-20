package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * Created by marcel on 20.11.15.
 */
public class Nop implements CrowdPlatform {
    @Override
    public void publishTask(String id, String name, String url, int amount) {
        System.out.println("Publish "+id+" name:"+name+" url:"+url+" amount:"+amount);
    }

    @Override
    public void updateTask(String id, String name, String url, int amount) {
        System.out.println("Update "+id+" name:"+name+" url:"+url+" amount:"+amount);
    }

    @Override
    public void unpublishTask(String id) {
        System.out.println("Unpublish "+id);
    }

    @Override
    public void payTask(String answer_id, int payment) {
        System.out.println("Pay "+answer_id+" with "+payment);

    }
}
