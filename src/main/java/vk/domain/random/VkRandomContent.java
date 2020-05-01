package vk.domain.random;

import vk.domain.vkObjects.VkAttachment;

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
