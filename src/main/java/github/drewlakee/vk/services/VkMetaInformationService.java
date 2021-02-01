package github.drewlakee.vk.services;

import com.google.gson.JsonElement;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VkMetaInformationService {

    private static final Logger log = LoggerFactory.getLogger(VkMetaInformationService.class);

    private final VkApiClient api;
    private final UserActor user;

    @Autowired
    public VkMetaInformationService(VkApiClient api, UserActor user) {
        this.api = api;
        this.user = user;
    }

    public int getGroupPostsCount(int groupId) {
        String request = String.format("return API.wall.get({\"owner_id\": %d}).count;", groupId * -1);

        int postsCount = 0;
        try {
            log.info("[VK] Request: {}", request);
            JsonElement responseCount = api.execute()
                    .code(user, request)
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
