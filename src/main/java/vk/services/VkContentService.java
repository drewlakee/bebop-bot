package vk.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import vk.domain.vkObjects.VkCustomAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VkContentService {

    private static final Logger log = LoggerFactory.getLogger(VkContentService.class);

    public static VkCustomAudio findRandomAudio() {
        Random random = new Random();
        int randomGroupId = VkGroupPool.getRandomAudioGroup().getId();
        int randomOffset = random.nextInt(VkInfoService.getGroupPostsCount(randomGroupId));
        int postsCount = 10;
        int requestLimit = 5;
        int requestCount = 0;

        List<JsonObject> jsonWallAudioObjects;
        do {
            JsonElement jsonWallPostsAttachments = getJsonWallPosts(postsCount, randomOffset, randomGroupId);
            jsonWallAudioObjects = getJsonAudioObjects(jsonWallPostsAttachments);

            if (requestCount > requestLimit) {
                break;
            }
            requestCount++;

            if (jsonWallAudioObjects.isEmpty()) {
                requestDelay();
                randomGroupId = VkGroupPool.getRandomAudioGroup().getId();
                randomOffset = random.nextInt(VkInfoService.getGroupPostsCount(randomGroupId));
            }
        } while (jsonWallAudioObjects.isEmpty());

        VkCustomAudio randomAudio = new VkCustomAudio();
        int randomAudioJsonObject;
        JsonObject audioJsonObject;
        if (requestCount < requestLimit) {
            randomAudioJsonObject = random.nextInt(jsonWallAudioObjects.size());
            audioJsonObject = jsonWallAudioObjects.get(randomAudioJsonObject);
            randomAudio.setArtist(audioJsonObject.get("artist").getAsString());
            randomAudio.setTitle(audioJsonObject.get("title").getAsString());
            randomAudio.setOwnerId(audioJsonObject.get("owner_id").getAsInt());
            randomAudio.setId(audioJsonObject.get("id").getAsInt());
        }

        return randomAudio;
    }

    private static JsonElement getJsonWallPosts(int postsCount, int offset, int ownerId) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        UserActor userActor = new UserActor(VkDefaultApiCredentials.userId, VkDefaultApiCredentials.token);
        String request = String.format("return API.wall.get({\"count\": %d, \"offset\": %d,  \"owner_id\": %d}).items@.attachments;", postsCount, offset, ownerId);

        JsonElement postsAttachments = new JsonObject();
        try {
            log.info("[VK] Request: " + request);
            postsAttachments = api.execute()
                    .code(userActor, request)
                    .execute();
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request response: " + request + " failed.");
            e.printStackTrace();
        }

        return postsAttachments;
    }

    private static List<JsonObject> getJsonAudioObjects(JsonElement jsonPosts) {
        List<JsonObject> audios = new ArrayList<>();

        for (JsonElement post : jsonPosts.getAsJsonArray()) {
            if (!post.isJsonNull()) {
                for (JsonElement jsonElement : post.getAsJsonArray()) {
                    JsonObject audio = jsonElement.getAsJsonObject().getAsJsonObject("audio");
                    if (audio != null)
                        audios.add(audio);
                }
            }
        }

        return audios;
    }

    public static Photo findRandomPhoto() {
        Random random = new Random();
        int postsCount = 10;
        int requestCount = 0;
        int requestLimit = 5;
        int randomGroupId = VkGroupPool.getRandomPhotoGroup().getId();
        int randomOffset = random.nextInt(VkInfoService.getGroupPostsCount(randomGroupId));

        List<WallpostFull> wallPosts;
        do {
            wallPosts = getWallPosts(postsCount, randomOffset, randomGroupId);
            wallPosts = getPostsWithPhoto(wallPosts);

            if (requestCount > requestLimit)
                break;
            requestCount++;

            if (wallPosts.isEmpty()) {
                requestDelay();
                randomGroupId = VkGroupPool.getRandomPhotoGroup().getId();
                randomOffset = random.nextInt(VkInfoService.getGroupPostsCount(randomGroupId));
            }
        } while (wallPosts.isEmpty());

        Photo randomPhoto = new Photo();
        int randomPostIndex;
        int randomPhotoIndex;
        if (requestCount < requestLimit) {
            randomPostIndex = random.nextInt(wallPosts.size());
            List<WallpostAttachment> attachments = wallPosts.get(randomPostIndex).getAttachments();
            List<WallpostAttachment> photos = attachments
                    .stream()
                    .filter(attachment -> attachment.getPhoto() != null)
                    .collect(Collectors.toList());
            randomPhotoIndex = random.nextInt(photos.size());
            randomPhoto = photos.get(randomPhotoIndex).getPhoto();
        }

        return randomPhoto;
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
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request API response: get wall posts with count: {}, offset: {}, ownerId: {} - failed.", postsCount, offset, ownerId);
            e.printStackTrace();
        }

        return wallPosts;
    }

    private static List<WallpostFull> getPostsWithPhoto(List<WallpostFull> posts) {
        List<WallpostFull> postsWithPhoto = new ArrayList<>();
        boolean isPostHavePhotos;

        for (WallpostFull post : posts) {
            if (post.getAttachments() != null) {
                isPostHavePhotos = post.getAttachments().stream()
                        .anyMatch(attachment -> attachment.getPhoto() != null);
                if (isPostHavePhotos)
                    postsWithPhoto.add(post);
            }
        }

        return postsWithPhoto;
    }

    private static void requestDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
