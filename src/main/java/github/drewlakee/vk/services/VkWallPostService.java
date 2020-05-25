package github.drewlakee.vk.services;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import github.drewlakee.vk.api.VkApiCredentials;
import github.drewlakee.vk.api.VkApiDefaultCredentials;
import github.drewlakee.vk.domain.groups.VkCustomGroup;

import java.util.List;

public class VkWallPostService {

    private static final Logger log = LoggerFactory.getLogger(VkWallPostService.class);

    public boolean makePost(VkCustomGroup group, List<String> attachments) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        VkApiCredentials credentials = new VkApiDefaultCredentials();
        UserActor userActor = new UserActor(credentials.getUserId(), credentials.getUserToken());
        boolean isOk = true;

        try {
            log.info("[VK] Request: groupId: {}, attachments: {}", group, attachments);
            api.wall()
                    .post(userActor)
                    .ownerId(group.getId())
                    .attachments(attachments)
                    .execute();
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request FAILED: group: {}, attachments: {}.", group, attachments);
            e.printStackTrace();

            isOk = false;
        }

        return isOk;
    }
}
