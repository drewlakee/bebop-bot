package vk.services;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vk.api.VkDefaultApiCredentials;
import vk.domain.groups.VkCustomGroup;

import java.util.List;

public class VkWallPostService {

    private static final Logger log = LoggerFactory.getLogger(VkWallPostService.class);

    public boolean sendWallPost(VkCustomGroup group, List<String> attachments) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        UserActor userActor = new UserActor(VkDefaultApiCredentials.userId, VkDefaultApiCredentials.token);
        boolean isOk = true;

        try {
            log.info("[VK] Request: wall post with params: groupId: {}, attachments: {}", group, attachments);
            api.wall()
                    .post(userActor)
                    .ownerId(group.getId())
                    .attachments(attachments)
                    .execute();
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request: wall post with params: group: {}, attachments: {} - failed.", group, attachments);
            e.printStackTrace();

            isOk = false;
        }

        return isOk;
    }
}
