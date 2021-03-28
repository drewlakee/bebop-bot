package github.drewlakee.vk.services.content;

import github.drewlakee.vk.domain.attachments.VkAttachment;

import java.util.List;

public interface VkContentSearchStrategy {

    List<VkAttachment> search(int quantity);
}
