package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.objects.groups.Group;
import lombok.Getter;

@Getter
public class VkCustomGroup extends Group {

    private VkGroupObjective vkGroupObjective;
    private String url;

    public VkCustomGroup(VkGroupObjective vkGroupObjective, String name, int groupId, String url) {
        setName(name);
        setId(groupId);
        this.vkGroupObjective = vkGroupObjective;
        this.url = url;
    }
}
