package github.drewlakee.vk.services.random;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import github.drewlakee.vk.api.VkApiCredentials;
import github.drewlakee.vk.api.VkApiDefaultCredentials;
import github.drewlakee.vk.domain.groups.VkCustomGroup;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.singletons.VkGroupPool;
import github.drewlakee.vk.domain.vkObjects.VkAttachment;
import github.drewlakee.vk.domain.vkObjects.VkCustomPhoto;
import github.drewlakee.vk.services.VkMetaInformationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VkRandomPhotoContent implements VkRandomContent {

    private static final Logger log = LoggerFactory.getLogger(VkRandomPhotoContent.class);

    @Override
    public List<VkAttachment> find(int quantity) {
        Random random = new Random();
        int requestLimit = (quantity == 1) ? 5 : quantity;
        int requestCount = 0;
        List<WallpostFull> wallPosts;
        List<Photo> photoAttachments = new ArrayList<>();
        List<VkCustomGroup> photoGroups = VkGroupPool.getConcreteGroups(VkGroupObjective.PHOTO);

        do {
            int randomIndex = random.nextInt(photoGroups.size());
            int randomGroupId = photoGroups.get(randomIndex).getId();
            int randomOffset = random.nextInt(VkMetaInformationService.getGroupPostsCount(randomGroupId));

            wallPosts = getWallPosts(quantity, randomOffset, randomGroupId);
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
                VkCustomPhoto photo = new VkCustomPhoto();
                photo.setId(photoAttachments.get(i).getId());
                photo.setOwnerId(photoAttachments.get(i).getOwnerId());
                photo.setSizes(photoAttachments.get(i).getSizes());
                photosResponse.add(photo);
            }
        }

        return photosResponse;
    }

    private static List<WallpostFull> getWallPosts(int postsCount, int offset, int ownerId) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        VkApiCredentials credentials = new VkApiDefaultCredentials();
        UserActor userActor = new UserActor(credentials.getUserId(), credentials.getUserToken());

        List<WallpostFull> wallPosts = new ArrayList<>();
        try {
            log.info("[VK] Request API: count: {}, offset: {}, ownerId: {}", postsCount, offset, ownerId);
            wallPosts = api.wall()
                    .get(userActor)
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

    private static List<Photo> getPhotoAttachments(List<WallpostFull> posts) {
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
