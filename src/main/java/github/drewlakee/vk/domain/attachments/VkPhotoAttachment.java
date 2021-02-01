package github.drewlakee.vk.domain.attachments;

import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;

public class VkPhotoAttachment extends Photo implements VkAttachment {

    public PhotoSizes getLargestSize() {
        return this.getSizes().get(this.getSizes().size() - 1);
    }

    @Override
    public String toAttachmentString() {
        return "photo" + this.getOwnerId() + "_" + this.getId();
    }
}
