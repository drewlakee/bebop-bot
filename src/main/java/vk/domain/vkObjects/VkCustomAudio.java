package vk.domain.vkObjects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.annotations.Required;
import com.vk.api.sdk.objects.audio.Audio;

public class VkCustomAudio extends Audio {

    /**
     * Owner ID
     */
    @SerializedName("owner_id")
    @Required
    private Integer ownerId;

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public String toPrettyString() {
        return getArtist() + " - " + getTitle();
    }
}
