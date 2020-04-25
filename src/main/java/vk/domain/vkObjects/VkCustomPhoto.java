package vk.domain.vkObjects;

import com.vk.api.sdk.objects.photos.Photo;

public class VkCustomPhoto extends Photo implements VkAttachment {

    @Override
    public String toAttachmentString() {
        return "photo" + this.getOwnerId() + "_" + this.getId();
    }
}
