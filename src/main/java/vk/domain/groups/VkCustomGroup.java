package vk.domain.groups;

import com.vk.api.sdk.objects.groups.Group;

public class VkCustomGroup extends Group {

    private final GroupObjective groupObjective;
    private final String url;

    public VkCustomGroup(GroupObjective groupObjective, String name, int groupId, String url) {
        setName(name);
        setId(groupId);
        this.groupObjective = groupObjective;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public GroupObjective getGroupObjective() {
        return groupObjective;
    }
}
