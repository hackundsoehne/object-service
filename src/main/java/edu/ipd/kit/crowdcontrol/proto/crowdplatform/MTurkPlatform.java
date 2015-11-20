package edu.ipd.kit.crowdcontrol.proto.crowdplatform;

/**
 * Created by marcel on 20.11.15.
 */
public class MTurkPlatform implements CrowdPlatform {
    private final String password;
    private final String username;
    private final String url;

    public MTurkPlatform(String username, String password, String url) {
        this.password = password;
        this.username = username;
        this.url = url;
    }

    @Override
    public boolean publishTask(String id, String name, String url, int amount) {

    }

    @Override
    public boolean updateTask(String id, String name, String url, int amount) {

    }

    @Override
    public boolean unpublishTask(String id) {

    }

    @Override
    public boolean payTask(String id, int payment) {

    }

    @Override
    public String getName() {
        return null;
    }
}
