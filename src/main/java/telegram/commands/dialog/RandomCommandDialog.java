package telegram.commands.dialog;

import com.vk.api.sdk.objects.photos.Photo;
import vk.domain.groups.VkGroup;
import vk.domain.vkObjects.VkCustomAudio;

import javax.annotation.concurrent.GuardedBy;

public class RandomCommandDialog extends Dialog {

    @GuardedBy("this")
    private String constructMode;

    @GuardedBy("this")
    private Photo photo;

    @GuardedBy("this")
    private VkCustomAudio audio;

    @GuardedBy("this")
    private VkGroup vkGroup;

    public RandomCommandDialog(int messageId) {
        super(messageId);
    }

    public synchronized void setConstructMode(String constructMode) {
        this.constructMode = constructMode;
    }

    public synchronized void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public synchronized void setAudio(VkCustomAudio audio) {
        this.audio = audio;
    }

    public synchronized void setVkGroup(VkGroup vkGroup) {
        this.vkGroup = vkGroup;
    }

    public synchronized String getConstructMode() {
        return constructMode;
    }

    public synchronized Photo getPhoto() {
        return photo;
    }

    public synchronized VkCustomAudio getAudio() {
        return audio;
    }

    public synchronized VkGroup getVkGroup() {
        return vkGroup;
    }

    public synchronized boolean hasPhotoChooseAnswer() {
        return constructMode != null;
    }

    public synchronized boolean hasPhoto() {
        return photo != null;
    }

    public synchronized boolean hasVkGroup() {
        return vkGroup != null;
    }

}
