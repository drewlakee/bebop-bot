package telegram.commands.dialog;

import com.vk.api.sdk.objects.photos.Photo;
import vk.domain.groups.VkGroup;

public class RandomCommandDialog extends Dialog {

    private Photo photo;
    private VkGroup vkGroup;

    public RandomCommandDialog(Integer messageId) {
        super(messageId);
    }

    public Photo getPhoto() {
        return photo;
    }

    public VkGroup getVkGroup() {
        return vkGroup;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public void setVkGroup(VkGroup vkGroup) {
        this.vkGroup = vkGroup;
    }
}
