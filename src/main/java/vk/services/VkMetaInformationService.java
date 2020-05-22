package vk.services;

import com.google.gson.JsonElement;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vk.api.VkApiCredentials;
import vk.api.VkApiDefaultCredentials;

public class VkMetaInformationService {

    private static final Logger log = LoggerFactory.getLogger(VkMetaInformationService.class);

    public static int getGroupPostsCount(int groupId) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        VkApiCredentials credentials = new VkApiDefaultCredentials();
        UserActor userActor = new UserActor(credentials.getUserId(), credentials.getUserToken());
        String request = String.format("return API.wall.get({\"owner_id\": %d}).count;", groupId);

        int postsCount = 0;
        try {
            log.info("[VK] Request: {}", request);
            JsonElement responseCount = api.execute()
                    .code(userActor, request)
                    .execute();
            postsCount = responseCount.getAsInt();
            log.info("[VK] Response: request - {}, response count - {}", request, postsCount);
        } catch (ClientException | ApiException e) {
            log.info("[VK] FAILED Request: {}", request);
            e.printStackTrace();
        }

        return postsCount;
    }
}
