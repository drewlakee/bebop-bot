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

public class VkInformationFinder {

    public static LocalDateTime getLastPostDate(int groupId) {
        LocalDateTime date = LocalDateTime.MIN;
        Wallpost wallpost;

        try {
            GetResponse response = VkApi.instance()
                    .wall()
                    .get(VkUserActor.instance())
                    .ownerId(groupId)
                    .offset(0)
                    .count(1)
                    .execute();

            wallpost = response.getItems().get(0);
            date = LocalDateTime.ofInstant(Instant.ofEpochSecond(wallpost.getDate()), ZoneId.systemDefault());
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }

        return date;
    }

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
