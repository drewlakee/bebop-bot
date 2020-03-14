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
        int randomOffset = random.nextInt(1000);
        int randomGroupId = VkGroupPool.getRandomAudioGroup().getGroupId();
        int postsCount = 10;
        int requestLimit = 5;
        int requestCount = 0;
        JsonElement jsonWallPostsAttachments;
        List<JsonObject> jsonWallAudioObjects;

        do {
            jsonWallPostsAttachments = getJsonWallPosts(postsCount, randomOffset, randomGroupId);
            jsonWallAudioObjects = getJsonAudioObjects(jsonWallPostsAttachments);
            randomGroupId = VkGroupPool.getRandomAudioGroup().getGroupId();

            requestCount++;
            if (requestCount > requestLimit)
                break;

            if (jsonWallAudioObjects.isEmpty())
                requestDelay();
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
        int randomOffset = random.nextInt(1000);
        int postsCount = 10;
        int requestCount = 0;
        int requestLimit = 5;
        int randomGroupId = VkGroupPool.getRandomPhotoGroup().getGroupId();
        List<WallpostFull> wallPosts;

        do {
            wallPosts = getWallPosts(postsCount, randomOffset, randomGroupId);
            wallPosts = getPostsWithPhoto(wallPosts);
            randomGroupId = VkGroupPool.getRandomPhotoGroup().getGroupId();

            requestCount++;
            if (requestCount > requestLimit)
                break;

            if (wallPosts.isEmpty())
                requestDelay();
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
