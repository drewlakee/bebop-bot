package github.drewlakee.vk.services.random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import github.drewlakee.vk.domain.attachments.VkAttachment;
import github.drewlakee.vk.domain.attachments.VkAudioAttachment;
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
public class VkRandomAudioContent implements VkRandomContent {

    private static final Logger log = LoggerFactory.getLogger(VkRandomAudioContent.class);

    private final VkGroupsCustodian custodian;

    private final VkApiClient api;
    private final UserActor user;

    private VkMetaInformationService metaInformationService;

    @Autowired
    public VkRandomAudioContent(VkGroupsCustodian custodian, VkApiClient api, UserActor user, VkMetaInformationService metaInformationService) {
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
        List<JsonObject> jsonAudioAttachments = new ArrayList<>();
        List<VkGroupFullDecorator> audioGroups = custodian.getConcreteObjectiveGroups(VkGroupObjective.AUDIO);

        do {
            int randomIndex = random.nextInt(audioGroups.size());
            int randomGroupId = audioGroups.get(randomIndex).getGroupFull().getId();
            int randomOffset = random.nextInt(metaInformationService.getGroupPostsCount(randomGroupId));

            JsonElement jsonWallPostsAttachments = getJsonWallPostAttachments(quantity, randomOffset, randomGroupId);
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
                VkAudioAttachment audio = new VkAudioAttachment();
                audio.setArtist(jsonAudioAttachments.get(i).get("artist").getAsString());
                audio.setTitle(jsonAudioAttachments.get(i).get("title").getAsString());
                audio.setOwnerId(jsonAudioAttachments.get(i).get("owner_id").getAsInt());
                audio.setId(jsonAudioAttachments.get(i).get("id").getAsInt());
                responseAudios.add(audio);
            }
        } else {
            log.info("[VK] Request [AUDIO]: Requests limit is exhausted");
        }

        return responseAudios;
    }

    private JsonElement getJsonWallPostAttachments(int quantity, int offset, int ownerId) {
        String request = String.format("return API.wall.get({\"count\": %d, \"offset\": %d,  \"owner_id\": %d}).items@.attachments;", quantity, offset, ownerId * -1);

        JsonElement postAttachments = new JsonObject();
        try {
            log.info("[VK] Request: " + request);
            postAttachments = api.execute()
                    .code(user, request)
                    .execute();
            log.info("[VK] Response: request - {}, response - {}", request, postAttachments);
        } catch (ClientException | ApiException e) {
            log.info("[VK] Request [FAILED]: {}", request);
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
