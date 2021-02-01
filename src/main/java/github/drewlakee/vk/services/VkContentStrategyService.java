package github.drewlakee.vk.services;

import github.drewlakee.vk.domain.attachments.VkAttachment;
import github.drewlakee.vk.services.random.VkRandomContent;

import java.util.Collections;
import java.util.List;

public class VkContentStrategyService {

    private final VkRandomContent content;

    public VkContentStrategyService(VkRandomContent content) {
        this.content = content;
    }

    public List<VkAttachment> find(int quantity) {
        if (quantity > 0) {
            return content.find(quantity);
        } else {
            return Collections.emptyList();
        }
    }
}
