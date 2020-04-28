package vk.domain.random;

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
import vk.api.VkDefaultApiCredentials;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkAttachment;
import vk.domain.vkObjects.VkCustomPhoto;
import vk.services.VkInfoService;

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
        int randomGroupId;
        int randomOffset;
        List<WallpostFull> wallPosts;
        List<Photo> photoAttachments = new ArrayList<>();

        do {
            randomGroupId = VkGroupPool.getRandomPhotoGroup().getId();
            randomOffset = random.nextInt(VkInfoService.getGroupPostsCount(randomGroupId));

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
        if (requestCount < requestLimit) {
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
        UserActor userActor = new UserActor(VkDefaultApiCredentials.userId, VkDefaultApiCredentials.token);

        List<WallpostFull> wallPosts = new ArrayList<>();
        try {
            log.info("[VK] Request API: get wall posts with count: {}, offset: {}, ownerId: {}", postsCount, offset, ownerId);
            wallPosts = api.wall()
                    .get(userActor)
                    .count(postsCount)
                    .offset(offset)
                    .ownerId(ownerId)
                    .execute()
                    .getItems();
            log.info("[VK] Response API: get wall posts with count: {}, offset: {}, ownerId: {}, response - {}", postsCount, offset, ownerId, wallPosts);
        } catch (ClientException | ApiException e) {
            log.info("[VK] FAILED Request API: get wall posts with count: {}, offset: {}, ownerId: {}.", postsCount, offset, ownerId);
            e.printStackTrace();
        }

        return wallPosts;
    }

    private static List<Photo> getPhotoAttachments(List<WallpostFull> posts) {
        List<Photo> postsWithPhoto = new ArrayList<>();

        for (WallpostFull post : posts) {
            if (post.getAttachments() != null) {
                for (WallpostAttachment attachment : post.getAttachments()) {
                    if (attachment.getPhoto() != null) {
                        postsWithPhoto.add(attachment.getPhoto());
                        break; // get one photo from every post
                    }
                }
            }
        }

        return postsWithPhoto;
    }

}
