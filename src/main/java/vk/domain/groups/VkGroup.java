package vk.domain.groups;

public class VkGroup {

    private final GroupObjective groupObjective;
    private final String name;
    private final int groupId;
    private final String url;

    public VkGroup(GroupObjective groupObjective, String name, int groupId, String url) {
        this.groupObjective = groupObjective;
        this.name = name;
        this.groupId = groupId;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getUrl() {
        return url;
    }

    public GroupObjective getGroupObjective() {
        return groupObjective;
    }
}
