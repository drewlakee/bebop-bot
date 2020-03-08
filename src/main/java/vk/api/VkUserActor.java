package vk.api;

import com.vk.api.sdk.client.actors.UserActor;

public class VkUserActor {

    private static UserActor actor;

    public static UserActor instance() {
        if (isEmpty())
            actor = new UserActor(
                    Integer.parseInt(System.getenv("vk_user_id")),
                    System.getenv("vk_token")
            );

        return actor;
    }

    private static boolean isEmpty() {
        return actor == null;
    }
}
