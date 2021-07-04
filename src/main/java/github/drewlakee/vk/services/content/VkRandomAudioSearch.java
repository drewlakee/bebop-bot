package github.drewlakee.vk.services.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.responses.GetExtendedResponse;
import github.drewlakee.vk.domain.attachments.VkAttachment;
import github.drewlakee.vk.domain.attachments.VkAudioAttachment;
import github.drewlakee.vk.domain.groups.VkGroupFullWrapper;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VkRandomAudioSearch implements VkContentSearchStrategy {

    private static final Logger log = LoggerFactory.getLogger(VkRandomAudioSearch.class);

    private static final Random random = new Random();

    private final VkGroupsCustodian custodian;
    private final VkApiClient api;
    private final UserActor user;

    @Autowired
    public VkRandomAudioSearch(VkGroupsCustodian custodian, VkApiClient api, UserActor user) {
        this.custodian = custodian;
        this.api = api;
        this.user = user;
    }

    @Override
    public List<VkAttachment> search(int quantity) {
        List<VkGroupFullWrapper> audioGroups = custodian.getConcreteObjectiveGroups(VkGroupObjective.AUDIO);
        int randomGroupIndex = random.nextInt(audioGroups.size());
        VkGroupFullWrapper randomGroup = audioGroups.get(randomGroupIndex);
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

        int quantityAmplifier = 10;
        List<JsonObject> filteredAudioAttachments = filterJsonAudioAttachments(
                getJsonWallPostAttachments(quantity * quantityAmplifier, randomOffset, randomGroup.getGroupFull().getId())
        );
        Collections.shuffle(filteredAudioAttachments);
        filteredAudioAttachments.stream()
                .map(this::toVkAudioAttachment)
                .limit(quantity)
                .forEach(attachments::add);

        return attachments;
    }

    private List<JsonObject> filterJsonAudioAttachments(JsonElement jsonPostAttachments) {
        List<JsonObject> jsonAudiosAttachments = new ArrayList<>();

        for (JsonElement jsonPost : jsonPostAttachments.getAsJsonArray()) {
            if (!jsonPost.isJsonNull()) {
                for (JsonElement jsonAttachment : jsonPost.getAsJsonArray()) {
                    JsonObject asJsonObject = jsonAttachment.getAsJsonObject();
                    if (asJsonObject.has("type") && asJsonObject.get("type").getAsString().equals("audio")) {
                        jsonAudiosAttachments.add(asJsonObject.getAsJsonObject("audio"));
                    }
                }
            }
        }

        return jsonAudiosAttachments;
    }

    private JsonElement getJsonWallPostAttachments(int quantity, int offset, int ownerId) {
        String request = String.format("return API.wall.get({\"count\": %d, \"offset\": %d,  \"owner_id\": %d}).items@.attachments;", quantity, offset, ownerId);

        JsonElement postAttachments = new JsonObject();
        try {
            postAttachments = api.execute()
                    .code(user, request)
                    .execute();
        } catch (ClientException | ApiException e) {
            log.info("VK API CALL EXECUTE METHOD was crashed cause {0}", e.getCause());
        }

        return postAttachments;
    }

    private VkAudioAttachment toVkAudioAttachment(JsonObject jsonAudio) {
        VkAudioAttachment audio = new VkAudioAttachment();
        audio.setArtist(jsonAudio.get("artist").getAsString());
        audio.setTitle(jsonAudio.get("title").getAsString());
        audio.setOwnerId(jsonAudio.get("owner_id").getAsInt());
        audio.setId(jsonAudio.get("id").getAsInt());
        return audio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VkRandomAudioSearch that = (VkRandomAudioSearch) o;
        return Objects.equals(custodian, that.custodian) && Objects.equals(api, that.api) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(custodian, api, user);
    }
}
