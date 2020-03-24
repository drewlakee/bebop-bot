package vk.services;

import com.google.gson.JsonElement;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import vk.api.VkApi;
import vk.api.VkUserActor;

public class VkInformationFinder {

    static int getGroupPostsCount(int groupId) {
        JsonElement responseCount;
        int postsCount = 0;

        try {
            String request = String.format("return API.wall.get({\"owner_id\": %d}).count;", groupId);
            responseCount = VkApi.instance()
                    .execute()
                    .code(VkUserActor.instance(), request)
                    .execute();

            postsCount = responseCount.getAsInt();
        } catch (ClientException | ApiException e) {
            e.printStackTrace();
        }

        return postsCount;
    }
}
