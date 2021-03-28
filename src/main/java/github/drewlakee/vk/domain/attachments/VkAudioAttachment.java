package github.drewlakee.vk.domain.attachments;

import com.vk.api.sdk.objects.audio.Audio;

import java.util.Objects;

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
    public String toPrettyVkAttachmentString() {
        return "audio" + this.getOwnerId() + "_" + this.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VkAudioAttachment that = (VkAudioAttachment) o;
        return Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ownerId);
    }
}
