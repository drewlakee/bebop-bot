package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.objects.groups.GroupFull;

import java.util.Objects;

public class VkGroupFullWrapper {

    private final VkGroupObjective objective;
    private final GroupFull groupFull;

    public VkGroupFullWrapper(GroupFull groupFull) {
        this.objective = VkGroupObjective.EMPTY;
        this.groupFull = groupFull;
    }

    public VkGroupFullWrapper(VkGroupObjective objective, GroupFull groupFull) {
        this.objective = objective;
        this.groupFull = groupFull;
    }

    public VkGroupObjective getObjective() {
        return objective;
    }

    public GroupFull getGroupFull() {
        return groupFull;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VkGroupFullWrapper that = (VkGroupFullWrapper) o;
        return objective == that.objective && Objects.equals(groupFull, that.groupFull);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objective, groupFull);
    }
}
