package vk.domain.groups;

public abstract class VkGroup {

    private final int groupId;

    public VkGroup(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }
}
