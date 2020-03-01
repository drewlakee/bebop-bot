package vk.domain.groups;

public class VkGroup {

    private final String name;
    private final int groupId;
    private final String url;

    public VkGroup(String name, int groupId, String url) {
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
}
