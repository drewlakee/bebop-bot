package vk.domain.random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vk.api.VkDefaultApiCredentials;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkAttachment;
import vk.domain.vkObjects.VkCustomAudio;
import vk.services.VkInfoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VkRandomAudioContent implements VkRandomContent {

    private static final Logger log = LoggerFactory.getLogger(VkRandomAudioContent.class);

    @Override
    public List<VkAttachment> find(int quantity) {
        Random random = new Random();
        int randomGroupId;
        int randomOffset;
        int requestLimit = (quantity == 1) ? 5 : quantity;
        int requestCount = 0;
        List<JsonObject> jsonAudioAttachments = new ArrayList<>();

        do {
            randomGroupId = VkGroupPool.getRandomAudioGroup().getId();
            randomOffset = random.nextInt(VkInfoService.getGroupPostsCount(randomGroupId));

            JsonElement jsonWallPostsAttachments = getJsonWallPost(quantity, randomOffset, randomGroupId);
            jsonAudioAttachments.addAll(getJsonAudioAttachments(jsonWallPostsAttachments));

            if (requestCount > requestLimit) {
                break;
            }

            requestCount++;
            if (jsonAudioAttachments.size() < quantity) {
                requestDelay();
            }
        } while (jsonAudioAttachments.size() < quantity);

        List<VkAttachment> responseAudios = new ArrayList<>();
        if (requestCount < requestLimit) {
            for (int i = 0; i < quantity; i++) {
                VkCustomAudio audio = new VkCustomAudio();
                audio.setArtist(jsonAudioAttachments.get(i).get("artist").getAsString());
                audio.setTitle(jsonAudioAttachments.get(i).get("title").getAsString());
                audio.setOwnerId(jsonAudioAttachments.get(i).get("owner_id").getAsInt());
                audio.setId(jsonAudioAttachments.get(i).get("id").getAsInt());
                responseAudios.add(audio);
            }
        } else {
            log.info("[VK] Request-audios: Requests limit is exhausted");
        }

        return responseAudios;
    }

    private static JsonElement getJsonWallPost(int quantity, int offset, int ownerId) {
        VkApiClient api = new VkApiClient(new HttpTransportClient());
        UserActor userActor = new UserActor(VkDefaultApiCredentials.userId, VkDefaultApiCredentials.token);
        String request = String.format("return API.wall.get({\"count\": %d, \"offset\": %d,  \"owner_id\": %d}).items@.attachments;", quantity, offset, ownerId);

        JsonElement postAttachments = new JsonObject();
        try {
            log.info("[VK] Request: " + request);
            postAttachments = api.execute()
                    .code(userActor, request)
                    .execute();
            log.info("[VK] Response: request - {}, response - {}", request, postAttachments);
        } catch (ClientException | ApiException e) {
            log.info("[VK] FAILED Request: {}", request);
            e.printStackTrace();
        }

        return postAttachments;
    }

    private static List<JsonObject> getJsonAudioAttachments(JsonElement jsonPostAttachments) {
        List<JsonObject> audiosAttachments = new ArrayList<>();

        for (JsonElement post : jsonPostAttachments.getAsJsonArray()) {
            if (!post.isJsonNull()) {
                for (JsonElement jsonElement : post.getAsJsonArray()) {
                    JsonObject audio = jsonElement.getAsJsonObject().getAsJsonObject("audio");
                    if (audio != null) {
                        audiosAttachments.add(audio);
                        break; // get one audio from every post
                    }
                }
            }
        }

        return audiosAttachments;
    }
}
