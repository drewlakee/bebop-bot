package github.drewlakee.vk.domain.groups;

import com.vk.api.sdk.objects.groups.GroupAdminLevel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class VkGroupsCustodian {

    private final Map<String, VkGroupFullWrapper> groups;

    public VkGroupsCustodian() {
        this.groups = new HashMap<>();
    }

    public void add(String screenName, VkGroupFullWrapper groupFullDecorator) {
        groups.put(screenName, groupFullDecorator);
    }

    public List<VkGroupFullWrapper> getGroupsWithEditableRights() {
        return groups.values().stream()
                .filter(group -> group.getGroupFull().getAdminLevel() == GroupAdminLevel.ADMINISTRATOR ||
                                 group.getGroupFull().getAdminLevel() == GroupAdminLevel.EDITOR)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<VkGroupFullWrapper> getConcreteObjectiveGroups(VkGroupObjective objective) {
        return groups.values().stream()
                .filter(group -> group.getObjective().equals(objective))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<VkGroupFullWrapper> getAllGroups() {
        return groups.values().stream()
                .sorted(Comparator.comparing(VkGroupFullWrapper::getObjective))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VkGroupsCustodian that = (VkGroupsCustodian) o;
        return Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups);
    }
}
