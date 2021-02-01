package github.drewlakee.vk.services.random;

import github.drewlakee.vk.domain.attachments.VkAttachment;

import java.util.List;

public interface VkRandomContent {

    List<VkAttachment> find(int quantity);

    default void requestDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
