package telegram.commands.dialog;

import com.vk.api.sdk.objects.photos.Photo;
import vk.domain.groups.VkGroup;

import javax.annotation.concurrent.GuardedBy;

public class RandomCommandDialog extends Dialog {

    @GuardedBy("this")
    private String photoChooseAnswer;

    @GuardedBy("this")
    private Photo photo;

    @GuardedBy("this")
    private VkGroup vkGroup;

    public RandomCommandDialog(int messageId) {
        super(messageId);
    }

    public synchronized void setPhotoChooseAnswer(String photoChooseAnswer) {
        this.photoChooseAnswer = photoChooseAnswer;
    }

    public synchronized void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public synchronized void setVkGroup(VkGroup vkGroup) {
        this.vkGroup = vkGroup;
    }

    public synchronized String getPhotoChooseAnswer() {
        return photoChooseAnswer;
    }

    public synchronized Photo getPhoto() {
        return photo;
    }

    public synchronized VkGroup getVkGroup() {
        return vkGroup;
    }

    public synchronized boolean hasPhotoChooseAnswer() {
        return photoChooseAnswer != null;
    }

    public synchronized boolean hasPhoto() {
        return photo != null;
    }

    public synchronized boolean hasVkGroup() {
        return vkGroup != null;
    }

}
