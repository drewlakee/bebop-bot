package vk.domain.groups;

public abstract class Group {

    private final long groupId;

    public Group(long groupId) {
        this.groupId = groupId;
    }

    public long getGroupId() {
        return groupId;
    }
}
