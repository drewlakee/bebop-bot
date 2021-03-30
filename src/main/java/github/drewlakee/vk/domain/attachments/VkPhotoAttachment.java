package github.drewlakee.vk.domain.attachments;

import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;

import java.util.Objects;

public class VkPhotoAttachment extends Photo implements VkAttachment {

    private String prettyVkAttachmentString;
    private String largestSizeUrl;

    public void setLargestSizeUrl(String largestUrl) {
        this.largestSizeUrl = largestUrl;
    }

    public void setPrettyVkAttachmentString(String prettyVkAttachmentString) {
        this.prettyVkAttachmentString = prettyVkAttachmentString;
    }

    public String getLargestSizeUrl() {
        if (largestSizeUrl != null) {
            return largestSizeUrl;
        } else {
            return this.getSizes().get(this.getSizes().size() - 1).getUrl().toString();
        }
    }

    @Override
    public String toPrettyVkAttachmentString() {
        if (prettyVkAttachmentString == null) {
            return "photo" + this.getOwnerId() + "_" + this.getId();
        } else {
            return prettyVkAttachmentString;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VkPhotoAttachment that = (VkPhotoAttachment) o;
        return Objects.equals(prettyVkAttachmentString, that.prettyVkAttachmentString) && Objects.equals(largestSizeUrl, that.largestSizeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), prettyVkAttachmentString, largestSizeUrl);
    }
}
