package github.drewlakee.vk.services.random;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import github.drewlakee.vk.domain.attachments.VkAttachment;
import github.drewlakee.vk.domain.attachments.VkPhotoAttachment;
import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import github.drewlakee.vk.services.VkMetaInformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class VkRandomPhotoContent implements VkRandomContent {

    private static final Logger log = LoggerFactory.getLogger(VkRandomPhotoContent.class);

    private final VkGroupsCustodian custodian;

    private final VkApiClient api;
    private final UserActor user;

    private VkMetaInformationService metaInformationService;

    @Autowired
    public VkRandomPhotoContent(VkGroupsCustodian custodian, VkApiClient api, UserActor user, VkMetaInformationService metaInformationService) {
        this.custodian = custodian;
        this.api = api;
        this.user = user;
        this.metaInformationService = metaInformationService;
    }

    @Override
    public List<VkAttachment> find(int quantity) {
        Random random = new Random();
        int requestLimit = (quantity == 1) ? 5 : quantity;
        int requestCount = 0;
        List<WallpostFull> wallPosts;
        List<Photo> photoAttachments = new ArrayList<>();
        List<VkGroupFullDecorator> photoGroups = custodian.getConcreteObjectiveGroups(VkGroupObjective.PHOTO);

        do {
            int randomIndex = random.nextInt(photoGroups.size());
            int randomGroupId = photoGroups.get(randomIndex).getGroupFull().getId();
            int randomOffset = random.nextInt(metaInformationService.getGroupPostsCount(randomGroupId));

            wallPosts = getWallPosts(quantity, randomOffset, randomGroupId * -1);
            photoAttachments.addAll(getPhotoAttachments(wallPosts));

            if (requestCount > requestLimit) {
                break;
            }

            requestCount++;
            if (photoAttachments.size() < quantity) {
                requestDelay();
            }
        } while (photoAttachments.size() < quantity);

        List<VkAttachment> photosResponse = new ArrayList<>();
        if (requestCount < requestLimit || photoAttachments.size() >= quantity) {
            for (int i = 0; i < quantity; i++) {
                VkPhotoAttachment photo = new VkPhotoAttachment();
                photo.setId(photoAttachments.get(i).getId());
                photo.setOwnerId(photoAttachments.get(i).getOwnerId());
                photo.setSizes(photoAttachments.get(i).getSizes());
                photosResponse.add(photo);
            }
        }

        return photosResponse;
    }

    private List<WallpostFull> getWallPosts(int postsCount, int offset, int ownerId) {
        List<WallpostFull> wallPosts = new ArrayList<>();
        try {
            log.info("[VK] Request API: count: {}, offset: {}, ownerId: {}", postsCount, offset, ownerId);
            wallPosts = api.wall()
                    .get(user)
                    .count(postsCount)
                    .offset(offset)
                    .ownerId(ownerId)
                    .execute()
                    .getItems();
            log.info("[VK] Response API: count: {}, offset: {}, ownerId: {}, response - {}", postsCount, offset, ownerId, wallPosts);
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request [API FAILED]: get wall posts with count: {}, offset: {}, ownerId: {}.", postsCount, offset, ownerId);
            e.printStackTrace();
        }

        return wallPosts;
    }

    private List<Photo> getPhotoAttachments(List<WallpostFull> posts) {
        List<Photo> postsWithPhoto = new ArrayList<>();

        // TODO: FIX BUG - NULL sometime happens
        posts.stream()
                .filter(post -> !post.getAttachments().isEmpty() && post.getAttachments().stream()
                        .anyMatch(attachment -> attachment.getPhoto() != null))
                .forEach(post -> {
                    for (WallpostAttachment attachment : post.getAttachments()) {
                        // (ownerId < 0) means that owner is a group
                        if (attachment.getPhoto().getOwnerId() < 0) {
                            postsWithPhoto.add(attachment.getPhoto());
                            break; // get one photo from every post
                        }
                    }
                });

        return postsWithPhoto;
    }

}
