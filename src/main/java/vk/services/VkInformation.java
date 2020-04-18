package vk.services;

import com.google.gson.JsonElement;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import vk.api.VkDefaultApiCredentials;

public class VkInformation {

    static int getGroupPostsCount(int groupId) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        UserActor userActor = new UserActor(VkDefaultApiCredentials.userId, VkDefaultApiCredentials.token);

        int postsCount = 0;
        try {
            String request = String.format("return API.wall.get({\"owner_id\": %d}).count;", groupId);
            JsonElement responseCount = api.execute()
                    .code(userActor, request)
                    .execute();
            postsCount = responseCount.getAsInt();
        } catch (ClientException | ApiException e) {
            e.printStackTrace();
        }

        return postsCount;
    }
}
