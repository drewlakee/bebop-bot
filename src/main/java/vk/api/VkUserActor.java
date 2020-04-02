package vk.api;

import app.AppEnvironmentProperties;
import com.vk.api.sdk.client.actors.UserActor;

public class VkUserActor {

    private static UserActor actor;

    public static UserActor instance() {
        if (isEmpty())
            actor = new UserActor(
                    Integer.parseInt(AppEnvironmentProperties.getAppProperty("vk_user_id")),
                    AppEnvironmentProperties.getAppProperty("vk_token")
            );

        return actor;
    }

    private static boolean isEmpty() {
        return actor == null;
    }
}
