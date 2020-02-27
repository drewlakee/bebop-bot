package vk;

import com.vk.api.sdk.client.actors.UserActor;

public class VkUserActor {

    private static UserActor actor;

    private static boolean isNull() {
        return actor == null;
    }

    public static UserActor instance(UserActor receivedActor) {
        if (isNull()) {
            actor = receivedActor;
        }

        return actor;
    }

    public static UserActor instance() {
        return instance(null);
    }
}
