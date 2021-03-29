package github.drewlakee.vk.services.content;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.responses.GetExtendedResponse;
import github.drewlakee.vk.domain.attachments.VkAttachment;
import github.drewlakee.vk.domain.attachments.VkPhotoAttachment;
import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class VkRandomPhotoSearch implements VkContentSearchStrategy {

    private static final Logger log = LoggerFactory.getLogger(VkRandomPhotoSearch.class);

    private static final Random random = new Random();

    private final VkGroupsCustodian custodian;
    private final VkApiClient api;
    private final UserActor user;

    @Autowired
    public VkRandomPhotoSearch(VkGroupsCustodian custodian, VkApiClient api, UserActor user) {
        this.custodian = custodian;
        this.api = api;
        this.user = user;
    }

    @Override
    public List<VkAttachment> search(int quantity) {
        List<VkGroupFullDecorator> photoGroups = custodian.getConcreteObjectiveGroups(VkGroupObjective.PHOTO);
        int randomGroupIndex = random.nextInt(photoGroups.size());
        VkGroupFullDecorator randomGroup = photoGroups.get(randomGroupIndex);
        List<VkAttachment> attachments = new ArrayList<>();

        if (quantity < 1) return attachments;

        GetExtendedResponse wallpostsResponse = new GetExtendedResponse();
        try {
            wallpostsResponse = api.wall()
                    .getExtended(user)
                    .ownerId(randomGroup.getGroupFull().getId())
                    .execute();
        } catch (ApiException | ClientException e) {
            log.warn("VK API CALL was crashed cause {0}", e.getCause());
        }

        if (wallpostsResponse.getCount() < 1) {
            return attachments;
        }

        int randomBound = wallpostsResponse.getCount() - quantity;
        if (randomBound < 1) randomBound = 0;
        int randomOffset = random.nextInt(randomBound);

        GetExtendedResponse randomWallpostsWithOffset = new GetExtendedResponse();
        try {
            randomWallpostsWithOffset = api.wall()
                    .getExtended(user)
                    .count(quantity)
                    .offset(randomOffset)
                    .ownerId(randomGroup.getGroupFull().getId())
                    .execute();
        } catch (ApiException | ClientException e) {
            log.warn("VK API CALL was crashed cause {0}", e.getCause());
        }

        randomWallpostsWithOffset
                .getItems()
                .stream()
                .flatMap(wallpost -> wallpost.getAttachments().stream())
                .filter(wallpostAttachment -> wallpostAttachment.getType() == WallpostAttachmentType.PHOTO)
                .map(WallpostAttachment::getPhoto)
                .limit(quantity)
                .forEach(photo -> {
                    VkPhotoAttachment vkPhotoAttachment = new VkPhotoAttachment();
                    vkPhotoAttachment.setId(photo.getId());
                    vkPhotoAttachment.setOwnerId(photo.getOwnerId());
                    vkPhotoAttachment.setSizes(photo.getSizes());
                    attachments.add(vkPhotoAttachment);
                });

        return attachments;
    }

}