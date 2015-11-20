package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * Created by marcel on 20.11.15.
 */
public class MTurkPlatform implements CrowdPlatform {
    private final String password;
    private final String username;
    private final String url;

    public MTurkPlatform(String password, String username, String url) {
        this.password = password;
        this.username = username;
        this.url = url;
    }

    @Override
    public void publishTask(String id, String name, String url, int amount) {
        Main
    }

    @Override
    public void updateTask(String id, String name, String url, int amount) {

    }

    @Override
    public void unpublishTask(String id) {

    }

    @Override
    public void payTask(String id, int payment) {

    }
}
