package github.drewlakee.vk.services;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VkWallPostService {

    private static final Logger log = LoggerFactory.getLogger(VkWallPostService.class);

    private final VkApiClient api;
    private final UserActor user;

    @Autowired
    public VkWallPostService(VkApiClient api, UserActor user) {
        this.api = api;
        this.user = user;
    }

    public boolean makePost(VkGroupFullDecorator group, List<String> attachments) {
        boolean isOk = true;

        try {
            log.info("[VK] Request: groupId: {}, attachments: {}", group.getGroupFull().getId(), attachments);
            api.wall()
                    .post(user)
                    .ownerId(group.getGroupFull().getId() * -1)
                    .attachments(attachments)
                    .execute();
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request FAILED: group: {}, attachments: {}.", group.getGroupFull().getId(), attachments);
            e.printStackTrace();

            isOk = false;
        }

        return isOk;
    }
}
