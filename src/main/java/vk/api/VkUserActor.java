package vk.api;

import app.AppEnvironment;
import com.vk.api.sdk.client.actors.UserActor;

public class VkUserActor {

    private static UserActor actor;

    public static UserActor instance() {
        if (isEmpty())
            actor = new UserActor(
                    Integer.parseInt(AppEnvironment.getAppProperty("vk_user_id")),
                    AppEnvironment.getAppProperty("vk_token")
            );

        return actor;
    }

    private static boolean isEmpty() {
        return actor == null;
    }
}
