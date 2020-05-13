package vk.services;

import vk.domain.vkObjects.VkAttachment;
import vk.domain.random.VkRandomContent;

import java.util.List;

public class VkContentService {

    private VkRandomContent content;

    public VkContentService(VkRandomContent content) {
        this.content = content;
    }

    public void setContent(VkRandomContent content) {
        this.content = content;
    }

    public List<VkAttachment> find(int quantity) {
        return content.find(quantity);
    }
}
