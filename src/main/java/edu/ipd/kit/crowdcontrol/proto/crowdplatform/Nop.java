package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

import java.util.concurrent.CompletableFuture;

/**
 * Created by marcel on 20.11.15.
 */
public class Nop implements CrowdPlatform {
    @Override
    public CompletableFuture<Boolean> publishTask(String id, String name, String url, int amount) {
        System.out.println("Publish "+id+" name:"+name+" url:"+url+" amount:"+amount);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateTask(String id, String name, String url, int amount) {
        System.out.println("Update "+id+" name:"+name+" url:"+url+" amount:"+amount);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> unpublishTask(String id) {
        System.out.println("Unpublish "+id);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> payTask(String answer_id, int payment) {
        System.out.println("Pay "+answer_id+" with "+payment);
        return CompletableFuture.completedFuture(true);
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
