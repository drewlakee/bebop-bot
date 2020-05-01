package vk.domain.groups;

import com.vk.api.sdk.objects.groups.Group;

public class VkCustomGroup extends Group {

    private final VkGroupObjective vkGroupObjective;
    private final String url;

    public VkCustomGroup(VkGroupObjective vkGroupObjective, String name, int groupId, String url) {
        setName(name);
        setId(groupId);
        this.vkGroupObjective = vkGroupObjective;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public VkGroupObjective getVkGroupObjective() {
        return vkGroupObjective;
    }
}
