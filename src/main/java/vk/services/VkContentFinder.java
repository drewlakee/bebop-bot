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
import vk.domain.VkGroupProvider;
import vk.domain.vkObjects.VkCustomAudio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VkContentFinder {

    public static VkCustomAudio findRandomAudio() {
        Random random = new Random();
        int randomOffset = random.nextInt(1000);
        int postsCount = 5;
        int randomGroupId = VkGroupProvider.getAudioGroupPool().getRandomGroup().getGroupId();
        JsonElement jsonWallPostsAttachments;
        List<JsonObject> jsonWallAudioObjects;

        do {
            jsonWallPostsAttachments = getJsonWallPosts(postsCount, randomOffset, randomGroupId);
            jsonWallAudioObjects = getJsonAudioObjects(jsonWallPostsAttachments);
            randomGroupId = VkGroupProvider.getAudioGroupPool().getRandomGroup().getGroupId();

            if (jsonWallAudioObjects.isEmpty())
                requestDelay();
        } while (jsonWallAudioObjects.isEmpty());

        int randomAudioJsonObject = random.nextInt(jsonWallAudioObjects.size());
        JsonObject audioJsonObject = jsonWallAudioObjects.get(randomAudioJsonObject);
        VkCustomAudio randomAudio = new VkCustomAudio();
        randomAudio.setOwnerId(audioJsonObject.get("owner_id").getAsInt());
        randomAudio.setId(audioJsonObject.get("id").getAsInt());

        return randomAudio;
    }

    private static JsonElement getJsonWallPosts(int postsCount, int offset, int ownerId) {
        JsonElement postsAttachments = new JsonObject();

        try {
            String request = String.format("return API.wall.get({\"count\": %d, \"offset\": %d,  \"owner_id\": %d}).items@.attachments;", postsCount, offset, ownerId);
            JsonElement response = VkApi.instance()
                    .execute()
                    .code(VkUserActor.instance(), request)
                    .execute();
            postsAttachments = response;
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
        int randomGroupId = VkGroupProvider.getPhotoGroupPool().getRandomGroup().getGroupId();
        List<WallpostFull> wallPosts;

        do {
            wallPosts = getWallPosts(postsCount, randomOffset, randomGroupId);
            wallPosts = getPostsWithPhoto(wallPosts);
            randomGroupId = VkGroupProvider.getPhotoGroupPool().getRandomGroup().getGroupId();

            if (wallPosts.isEmpty())
                requestDelay();
        } while (wallPosts.isEmpty());

        int randomPostIndex = random.nextInt(wallPosts.size());
        List<WallpostAttachment> attachments = wallPosts.get(randomPostIndex).getAttachments();
        List<WallpostAttachment> photos = attachments.stream()
                .filter(attachment -> attachment.getPhoto() != null)
                .collect(Collectors.toList());
        int randomPhotoIndex = random.nextInt(photos.size());

        return photos.get(randomPhotoIndex).getPhoto();
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
            isPostHavePhotos = post.getAttachments().stream()
                    .anyMatch(attachment -> attachment.getPhoto() != null);
            if (isPostHavePhotos)
                postsWithPhoto.add(post);
        }

        return postsWithPhoto;
    }

    private static void requestDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
