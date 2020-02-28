package vk;

import com.vk.api.sdk.client.actors.UserActor;

public class VkUserActor {

    private static UserActor actor;

    private static boolean isEmpty() {
        return actor == null;
    }

    public static void init(UserActor receivedActor) {
        if (isEmpty()) {
            actor = receivedActor;
        }
    }

    public static UserActor instance() {
        if (isEmpty())
            throw new NullPointerException();
        else
            return actor;
    }
}
