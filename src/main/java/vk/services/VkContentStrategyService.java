package vk.services;

import vk.domain.random.VkRandomContent;
import vk.domain.vkObjects.VkAttachment;

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
