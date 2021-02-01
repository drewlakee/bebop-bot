package github.drewlakee.vk.domain.attachments;

import com.vk.api.sdk.objects.audio.Audio;

public class VkAudioAttachment extends Audio implements VkAttachment {

    private Integer ownerId;

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String toPrettyString() {
        return getArtist() + " - " + getTitle();
    }

    @Override
    public String toAttachmentString() {
        return "audio" + this.getOwnerId() + "_" + this.getId();
    }
}
