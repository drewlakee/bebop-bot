package vk.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import vk.api.VkApi;
import vk.api.VkUserActor;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkCustomAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VkRandomContentFinder {

    public static VkCustomAudio findRandomAudio() {
        Random random = new Random();
        int randomGroupId = VkGroupPool.getRandomAudioGroup().getGroupId();
        int randomOffset = random.nextInt(VkInformationFinder.getGroupPostsCount(randomGroupId));
        int postsCount = 10;
        int requestLimit = 5;
        int requestCount = 0;
        JsonElement jsonWallPostsAttachments;
        List<JsonObject> jsonWallAudioObjects;

        do {
            jsonWallPostsAttachments = getJsonWallPosts(postsCount, randomOffset, randomGroupId);
            jsonWallAudioObjects = getJsonAudioObjects(jsonWallPostsAttachments);

            if (requestCount > requestLimit)
                break;
            requestCount++;

            if (jsonWallAudioObjects.isEmpty()) {
                requestDelay();
                randomGroupId = VkGroupPool.getRandomAudioGroup().getGroupId();
                randomOffset = random.nextInt(VkInformationFinder.getGroupPostsCount(randomGroupId));
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
        JsonElement postsAttachments = new JsonObject();

        try {
            String request = String.format("return API.wall.get({\"count\": %d, \"offset\": %d,  \"owner_id\": %d}).items@.attachments;", postsCount, offset, ownerId);
            postsAttachments = VkApi.instance()
                    .execute()
                    .code(VkUserActor.instance(), request)
                    .execute();
        } catch (ClientException | ApiException e) {
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
        int randomGroupId = VkGroupPool.getRandomPhotoGroup().getGroupId();
        int randomOffset = random.nextInt(VkInformationFinder.getGroupPostsCount(randomGroupId));
        List<WallpostFull> wallPosts;

        do {
            wallPosts = getWallPosts(postsCount, randomOffset, randomGroupId);
            wallPosts = getPostsWithPhoto(wallPosts);

            if (requestCount > requestLimit)
                break;
            requestCount++;

            if (wallPosts.isEmpty()) {
                requestDelay();
                randomGroupId = VkGroupPool.getRandomPhotoGroup().getGroupId();
                randomOffset = random.nextInt(VkInformationFinder.getGroupPostsCount(randomGroupId));
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
        List<WallpostFull> wallPosts = new ArrayList<>();

        try {
            wallPosts = VkApi.instance()
                    .wall()
                    .get(VkUserActor.instance())
                    .count(postsCount)
                    .offset(offset)
                    .ownerId(ownerId)
                    .execute()
                    .getItems();
        } catch (ClientException | ApiException e) {
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
