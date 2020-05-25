package github.drewlakee.vk.services;

import github.drewlakee.vk.services.random.VkRandomContent;
import github.drewlakee.vk.domain.vkObjects.VkAttachment;

import java.util.List;

public class VkContentStrategyService {

    private VkRandomContent content;

    public VkContentStrategyService(VkRandomContent content) {
        this.content = content;
    }

    public List<VkAttachment> find(int quantity) {
        return content.find(quantity);
    }
}
