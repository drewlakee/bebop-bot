package vk.services;

import com.google.gson.JsonElement;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.Wallpost;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import vk.api.VkApi;
import vk.api.VkUserActor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

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
